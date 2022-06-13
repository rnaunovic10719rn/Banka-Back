package rs.edu.raf.banka.berza.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.requests.TransakcijaKapitalType;
import rs.edu.raf.banka.berza.requests.TransakcijaRequest;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.response.TransakcijaResponse;
import rs.edu.raf.banka.berza.service.remote.TransakcijaService;
import rs.edu.raf.banka.berza.utils.HttpUtils;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class OrderService {

    private final Integer DEAFULT_BACKOFF = 2;

    private OrderRepository orderRepository;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;
    private PriceService priceService;
    private UserService userService;
    private TransakcijaService transakcijaService;

    private EntityManager entityManager;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        FuturesUgovoriPodaciService futuresUgovoriPodaciService,
                        PriceService priceService,
                        UserService userService,
                        TransakcijaService transakcijaService,
                        EntityManager entityManager
    ){
        this.orderRepository = orderRepository;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
        this.priceService = priceService;
        this.userService = userService;
        this.transakcijaService = transakcijaService;
        this.entityManager = entityManager;
    }

    private List<Order> getOrderNotDone() {
        return orderRepository.findOrderByDoneFalse();
    }

    public List<Order> getOrders(String token) {
        UserDto user = userService.getUserByToken(token);
        String role = userService.getUserRoleByToken(token);
        if (role.equals("ROLE_ADMIN")) {
            return orderRepository.findOrderByUserId(user.getId());
        }
        return orderRepository.findAll();
    }

    public List<Order> getOrders(String token, String status, Boolean done) {
        UserDto user = userService.getUserByToken(token);
        String role = userService.getUserRoleByToken(token);
        if(role.equals("ROLE_AGENT")) {
            return orderRepository.findOrderByUserIdAndDoneAndOrderStatus(user.getId(), done, OrderStatus.valueOf(status.toUpperCase()));
        }
        return orderRepository.findOrderByOrderStatusAndDone(OrderStatus.valueOf(status.toUpperCase()), done);
    }

    public ApproveRejectOrderResponse approveOrder(String userRole, Long id){
        if(!isSupervisor(userRole)) {
            return new ApproveRejectOrderResponse(MessageUtils.REQUIRED_PERMISSION);
        }

        Order order = orderRepository.getById(id);

        if (order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FUTURES_UGOVOR &&
                !futuresUgovoriPodaciService.isRelevant(order.getHartijaOdVrednostiId())) {
            return rejectOrder(userRole, id);
        }

        order.setOrderStatus(OrderStatus.APPROVED);
        order.setLastModified(new Date());
        orderRepository.save(order);

        return new ApproveRejectOrderResponse(MessageUtils.ORDER_APPROVED);
    }

    public ApproveRejectOrderResponse rejectOrder(String userRole, Long id){
        if(!isSupervisor(userRole)) {
            return new ApproveRejectOrderResponse(MessageUtils.REQUIRED_PERMISSION);
        }

        Order order = orderRepository.getById(id);
        order.setOrderStatus(OrderStatus.REJECTED);
        order.setLastModified(new Date());
        orderRepository.save(order);

        return new ApproveRejectOrderResponse(MessageUtils.ORDER_REJECTED);
    }

    private TransakcijaRequest getRezervacijaForOrder(Order order) {
        TransakcijaRequest transakcijaRequest = new TransakcijaRequest();

        if (order.getHartijaOdVrednosti() == HartijaOdVrednostiType.AKCIJA || order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FUTURES_UGOVOR) {
            // Type (Novac ili Hartija)
            if(order.getOrderAction() == OrderAction.BUY) {
                transakcijaRequest.setType(TransakcijaKapitalType.NOVAC);
            } else {
                switch (order.getHartijaOdVrednosti()) {
                    case AKCIJA -> transakcijaRequest.setType(TransakcijaKapitalType.AKCIJA);
                    case FUTURES_UGOVOR -> transakcijaRequest.setType(TransakcijaKapitalType.FUTURE_UGOVOR);
                }
            }

            // Opis i valuta
            if(order.getHartijaOdVrednosti() == HartijaOdVrednostiType.AKCIJA) {
                if(order.getOrderAction() == OrderAction.BUY) {
                    transakcijaRequest.setOpis("Rezervacija za kupovinu akcije " + order.getHartijaOdVrednostiSymbol());
                    transakcijaRequest.setValutaOznaka(order.getBerza().getValuta().getKodValute());
                } else {
                    transakcijaRequest.setOpis("Rezervacija za prodaju akcije " + order.getHartijaOdVrednostiSymbol());
                    transakcijaRequest.setHartijaId(order.getHartijaOdVrednostiId());
                }
            } else if(order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FUTURES_UGOVOR) {
                if(order.getOrderAction() == OrderAction.BUY) {
                    transakcijaRequest.setOpis("Rezervacija za kupovinu futures ugovora " + order.getHartijaOdVrednostiSymbol());
                    transakcijaRequest.setValutaOznaka("USD");
                } else {
                    transakcijaRequest.setOpis("Rezervacija za prodaju futures ugovora " + order.getHartijaOdVrednostiSymbol());
                    transakcijaRequest.setHartijaId(order.getHartijaOdVrednostiId());
                }
            }

            transakcijaRequest.setOrderId(order.getId());
            transakcijaRequest.setUplata(0.0);
            transakcijaRequest.setIsplata(0.0);

            if(order.getOrderAction() == OrderAction.BUY) {
                transakcijaRequest.setRezervisano(order.getPredvidjenaCena());
            } else {
                transakcijaRequest.setRezervisano(order.getKolicina());
            }

            transakcijaRequest.setLastSegment(false);

            return transakcijaRequest;
        } else if (order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FOREX) {
            String[] valute = order.getHartijaOdVrednostiSymbol().split(" ");
            transakcijaRequest.setType(TransakcijaKapitalType.NOVAC);
            transakcijaRequest.setOpis("Rezervacija za Forex transakciju " + order.getHartijaOdVrednostiSymbol());
            transakcijaRequest.setValutaOznaka(valute[0]);
            transakcijaRequest.setOrderId(order.getId());
            transakcijaRequest.setUplata(0.0);
            transakcijaRequest.setIsplata(0.0);
            transakcijaRequest.setRezervisano(order.getPredvidjenaCena());
            transakcijaRequest.setLastSegment(false);
            return transakcijaRequest;
        }

        return null;
    }

    private List<TransakcijaRequest> getTransakcijeForOrder(Order order, Integer kolicina, Boolean lastSegment) {
        List<TransakcijaRequest> transakcije = new ArrayList<>();

        if (order.getHartijaOdVrednosti() == HartijaOdVrednostiType.AKCIJA || order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FUTURES_UGOVOR) {
            if(order.getOrderAction() == OrderAction.BUY) {
                TransakcijaRequest novac = new TransakcijaRequest();
                novac.setType(TransakcijaKapitalType.NOVAC);
                novac.setOpis("Isplata sredstava za " + order.getHartijaOdVrednostiSymbol());
                novac.setValutaOznaka(order.getBerza().getValuta().getKodValute());
                novac.setOrderId(order.getId());
                novac.setUplata(0.0);
                novac.setIsplata(order.getAsk() * kolicina);
                novac.setRezervisano(0.0);
                novac.setLastSegment(lastSegment);
                transakcije.add(novac);

                TransakcijaRequest hartija = new TransakcijaRequest();
                switch (order.getHartijaOdVrednosti()) {
                    case AKCIJA -> hartija.setType(TransakcijaKapitalType.AKCIJA);
                    case FUTURES_UGOVOR -> hartija.setType(TransakcijaKapitalType.FUTURE_UGOVOR);
                }
                hartija.setOpis("Uplata " + order.getHartijaOdVrednostiSymbol());
                hartija.setValutaOznaka(order.getBerza().getValuta().getKodValute());
                hartija.setOrderId(order.getId());
                hartija.setHartijaId(order.getHartijaOdVrednostiId());
                hartija.setUplata(kolicina);
                hartija.setIsplata(0.0);
                hartija.setRezervisano(0.0);
                hartija.setLastSegment(false);
                hartija.setUnitPrice(order.getAsk());
                transakcije.add(hartija);
            } else {
                TransakcijaRequest hartija = new TransakcijaRequest();
                switch (order.getHartijaOdVrednosti()) {
                    case AKCIJA -> hartija.setType(TransakcijaKapitalType.AKCIJA);
                    case FUTURES_UGOVOR -> hartija.setType(TransakcijaKapitalType.FUTURE_UGOVOR);
                }
                hartija.setOpis("Isplata " + order.getHartijaOdVrednostiSymbol());
                hartija.setValutaOznaka(order.getBerza().getValuta().getKodValute());
                hartija.setOrderId(order.getId());
                hartija.setUplata(0.0);
                hartija.setIsplata(kolicina);
                hartija.setRezervisano(0.0);
                hartija.setLastSegment(lastSegment);
                hartija.setUnitPrice(order.getBid());
                transakcije.add(hartija);

                TransakcijaRequest novac = new TransakcijaRequest();
                novac.setType(TransakcijaKapitalType.NOVAC);
                novac.setOpis("Uplata sredstava od prodaje za " + order.getHartijaOdVrednostiSymbol());
                novac.setValutaOznaka(order.getBerza().getValuta().getKodValute());
                novac.setOrderId(order.getId());
                novac.setUplata(order.getBid() * kolicina);
                novac.setIsplata(0.0);
                novac.setRezervisano(0.0);
                novac.setLastSegment(false);
                transakcije.add(novac);
            }
        } else if (order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FOREX) {
            String[] valute = order.getHartijaOdVrednostiSymbol().split(" ");

            TransakcijaRequest prodaja = new TransakcijaRequest();
            prodaja.setType(TransakcijaKapitalType.NOVAC);
            prodaja.setOpis("Forex: prodaja " + valute[0]);
            prodaja.setValutaOznaka(valute[0]);
            prodaja.setOrderId(order.getId());
            prodaja.setUplata(0.0);
            prodaja.setIsplata(order.getAsk() * kolicina);
            prodaja.setRezervisano(0.0);
            prodaja.setLastSegment(lastSegment);
            transakcije.add(prodaja);

            TransakcijaRequest kupovina = new TransakcijaRequest();
            kupovina.setType(TransakcijaKapitalType.NOVAC);
            kupovina.setOpis("Forex: kupovina " + valute[1]);
            kupovina.setValutaOznaka(valute[1]);
            kupovina.setOrderId(order.getId());
            kupovina.setUplata(kolicina);
            kupovina.setIsplata(0.0);
            kupovina.setRezervisano(0.0);
            kupovina.setLastSegment(false);
            kupovina.setUnitPrice(order.getAsk());
            transakcije.add(kupovina);
        }

        return transakcije;
    }

    @Transactional
    public Order saveOrder(String token, OrderRequest orderRequest, Long userAccount, Berza berza, Long hartijaOdVrednostiId, HartijaOdVrednostiType hartijaOdVrednostiType,
                           OrderAction orderAction, Double ukupnaCena, Double provizija,
                           OrderType orderType, OrderStatus status){

        String username = userService.getUserByToken(token).getUsername();

        Order order = new Order();
        order.setUserId(userAccount);
        order.setUsername(username);
        order.setBerza(berza);
        order.setHartijaOdVrednostiId(hartijaOdVrednostiId);
        order.setHartijaOdVrednosti(hartijaOdVrednostiType);
        order.setHartijaOdVrednostiSymbol(orderRequest.getSymbol());
        order.setKolicina(orderRequest.getKolicina());
        order.setPreostalaKolicina(orderRequest.getKolicina());
        order.setOrderAction(orderAction);
        order.setPredvidjenaCena(ukupnaCena);
        order.setProvizija(provizija);
        order.setOrderType(orderType);
        order.setAON(orderRequest.isAllOrNoneFlag());
        order.setMargin(orderRequest.isMarginFlag());
        order.setLastModified(new Date());
        order.setOrderStatus(status);
        order.setLimitValue(orderRequest.getLimitValue());
        order.setStopValue(orderRequest.getStopValue());

        order = orderRepository.save(order);

        TransakcijaRequest transakcijaRequest = getRezervacijaForOrder(order);
        if(transakcijaRequest != null) {
            HttpUtils.retryTemplate().execute(context -> {
                TransakcijaResponse transakcijaResponse = transakcijaService.commitTransaction(token, transakcijaRequest);
                if(transakcijaResponse == null) {
                    // NB: Bitno kako bi rollbackovali order.
                    throw new RuntimeException("failed to commit transaction");
                }

                return null;
            });

        }

        return order;
    }

    /**
     * OBRADA ORDERA
     */

    @Scheduled(cron = "*/10 * * * * *") // Every 30 seconds
    @Transactional
    public void executeOrder() {
        List<Order> orders = getOrderNotDone();

        for(Order o: orders) {
            if(!o.getOrderStatus().equals(OrderStatus.APPROVED)) {
                log.info("Skipping order {} because it's not approved", o.getId());
                continue;
            }

            log.info("Executing order {}", o.getId());
            executeTransaction(o);
        }
    }

    @Transactional
    public void executeTransaction(final Order o){
        // Korak 0: Zakljucaj Order.
        Order order = entityManager.find(Order.class, o.getId(), LockModeType.PESSIMISTIC_WRITE);
        if(order == null) {
            throw new RuntimeException("failed to get and lock order");
        }

        // Korak 1: Ako je order na berzi, proveri da li je berza otvorena
        if(order.getBerza() != null){
            BerzaStatus orderStatus = getBerzaStatus(order.getBerza());
            Boolean berzaOpen = orderStatus.isBerzaOtvorena();
            // Ako berza nije otvorena, uradi backoff simulacija, tako da se order izvrsi tek posle n ciklusa
            if(!berzaOpen) {
                if(order.getBackoff() == -1) {
                    order.setBackoff(DEAFULT_BACKOFF);
                    orderRepository.save(order);
                    return;
                } else if(order.getBackoff() > 0) {
                    order.setBackoff(order.getBackoff() - 1);
                    orderRepository.save(order);
                    return;
                }
            }
        }

        // Korak 2: Izracunaj kolicinu tog dela ordera
        Random random = new Random();
        int kolicina = order.getPreostalaKolicina();
        int kolicinaZaTransakciju = random.nextInt(kolicina) + 1;
        // Ukoliko je order AON (All or None), izvrsi sve odjednom.
        if (order.isAON()) {
            kolicinaZaTransakciju = order.getKolicina();
        }

        // Korak 3: Odredi cenu za taj order
        log.info("Getting prices for order {}", order.getId());
        AskBidPriceDto askBidPrice = priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol());
        order.setAsk(askBidPrice.getAsk());
        order.setBid(askBidPrice.getBid());

        // Korak 4: Provera da li order moze da se izvsi (relevatno za STOP, LIMIT i STOP_LIMIT ordere)
        if(order.getOrderAction().equals(OrderAction.BUY) && !canExecuteTransactionBuy(order))
            return;
        if(order.getOrderAction().equals(OrderAction.SELL) && !canExecuteTransactionSell(order))
            return;

        // Korak 5: Izvrsi order.
        Boolean lastSegment = false;
        int novaPreostalaKolicina = order.getPreostalaKolicina() - kolicinaZaTransakciju;
        if(novaPreostalaKolicina <= 0) {
            lastSegment = true;
            order.setDone(true);
            order.setLastModified(new Date());
        }

        log.info("Executing order {} {} for {} (remaining {})", order.getId(), order.getOrderAction(), kolicinaZaTransakciju, novaPreostalaKolicina);

        List<TransakcijaRequest> transakcije = getTransakcijeForOrder(order, kolicinaZaTransakciju, lastSegment);
        for(TransakcijaRequest tr: transakcije) {
            tr.setUsername(order.getUsername());
            HttpUtils.retryTemplate().execute(context -> {
                TransakcijaResponse transakcijaResponse = transakcijaService.commitTransaction("Bearer BERZA-SERVICE", tr);
                if(transakcijaResponse == null) {
                    // NB: Bitno kako bi rollbackovali order.
                    throw new RuntimeException("failed to commit transaction");
                }

                return null;
            });
        }

        order.setPreostalaKolicina(novaPreostalaKolicina);

        orderRepository.save(order);
    }

    public boolean canExecuteTransactionBuy(Order order){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getAsk() < order.getLimitValue())
                    return true;
                break;
            case STOP_ORDER:
                if(order.getAsk() > order.getStopValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getPredvidjenaCena() <= order.getLimitValue() && order.getPredvidjenaCena() < order.getBid()){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
                break;
            default:
                return true;
        }
        return false;
    }

    public boolean canExecuteTransactionSell(Order order){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getBid() > order.getLimitValue())
                    return true;
                break;
            case STOP_ORDER:
                if(order.getBid() < order.getStopValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getPredvidjenaCena() > order.getLimitValue() && order.getPredvidjenaCena() > order.getAsk()){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
            default:
                return true;
        }
        return false;
    }

    public BerzaStatus getBerzaStatus(Berza berza){
        Date date = new Date();

        String openTime = berza.getOpenTime();
        String closeTime = berza.getCloseTime();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        try {
            if(isOverlapping(dateFormat.parse(openTime), dateFormat.parse(closeTime), date))
                return new BerzaStatus(true, MessageUtils.ORDER_APPROVED);

            if(differenceInHours(dateFormat.parse(closeTime), date))
                return new BerzaStatus(false, MessageUtils.STOCK_AFTERHOURS);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new BerzaStatus(false, MessageUtils.STOCK_CLOSED);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class BerzaStatus {
        private boolean berzaOtvorena;
        private String poruka;
    }

    private boolean isOverlapping(Date start, Date end, Date timeToCheck){
        return start.before(timeToCheck) && end.after(timeToCheck);
    }

    public boolean differenceInHours(Date closingTime, Date timeToCheck){
        long differenceInMilliSeconds = timeToCheck.getTime() - closingTime.getTime();
        return (differenceInMilliSeconds / (60 * 60 * 1000)) % 24 <= 4;
    }

    private boolean isSupervisor(String userRole) {
        return UserRole.ROLE_SUPERVISOR.equals(UserRole.valueOf(userRole));
    }
}
