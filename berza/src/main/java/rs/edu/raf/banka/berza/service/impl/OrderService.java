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
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Autowired
    public OrderService(OrderRepository orderRepository, FuturesUgovoriPodaciService futuresUgovoriPodaciService, PriceService priceService, UserService userService){
        this.orderRepository = orderRepository;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
        this.priceService = priceService;
        this.userService = userService;
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

    public Order saveOrder(OrderRequest orderRequest, Long userAccount, Berza berza, Long hartijaOdVrednostiId, HartijaOdVrednostiType hartijaOdVrednostiType,
                           OrderAction orderAction, Double ukupnaCena, Double provizija,
                           OrderType orderType, OrderStatus status){
        Order order = new Order();
        order.setUserId(userAccount);
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

        return orderRepository.save(order);
    }

    /**
     * OBRADA ORDERA
     */

    @Scheduled(cron = "*/10 * * * * *") // Every 30 seconds
    public void executeOrder() {
        List<Order> orders = getOrderNotDone();

        for(Order o: orders) {
            if(!o.getOrderStatus().equals(OrderStatus.APPROVED)) {
                log.info("Skipping order {} because it's not approved", o.getId());
                return;
            }

            log.info("Getting prices for order {}", o.getId());
            AskBidPriceDto askBidPrice = priceService.getAskBidPrice(o.getHartijaOdVrednosti(), o.getHartijaOdVrednostiSymbol());
            o.setAsk(askBidPrice.getAsk());
            o.setBid(askBidPrice.getBid());

            log.info("Executing order {}", o.getId());
            executeTransaction(o);
        }
    }

    public void executeTransaction(Order order){
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

        // Korak 3: Provera da li order moze da se izvsi (relevatno za STOP, LIMIT i STOP_LIMIT ordere)
        if(order.getOrderAction().equals(OrderAction.BUY) && !canExecuteTransactionBuy(order))
            return;
        if(order.getOrderAction().equals(OrderAction.SELL) && !canExecuteTransactionSell(order))
            return;

        // Korak 4: Izvrsi order.
        int novaPreostalaKolicina = order.getPreostalaKolicina() - kolicinaZaTransakciju;

        log.info("Executing order {} {} for {} (remaining {})", order.getId(), order.getOrderAction(), kolicinaZaTransakciju, novaPreostalaKolicina);

        order.setPreostalaKolicina(novaPreostalaKolicina);
        if(novaPreostalaKolicina <= 0) {
            order.setDone(true);
            order.setLastModified(new Date());
        }
        orderRepository.save(order);
    }

    public boolean canExecuteTransactionBuy(Order order){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getPredvidjenaCena() <= order.getLimitValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getPredvidjenaCena() <= order.getLimitValue() && order.getPredvidjenaCena() < order.getBid()){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
                break;
            case STOP_ORDER:
                if(order.getPredvidjenaCena() < order.getBid())
                    return true;
                break;
            default:
                return true;
        }
        return false;
    }

    public boolean canExecuteTransactionSell(Order order){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getPredvidjenaCena() > order.getLimitValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getPredvidjenaCena() > order.getLimitValue() && order.getPredvidjenaCena() > order.getAsk()){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
            case STOP_ORDER:
                if(order.getPredvidjenaCena() > order.getAsk())
                    return true;
                break;
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
