package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.model.Transakcija;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.response.OrderStatusResponse;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    public static Long berzaId;

    private OrderRepository orderRepository;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;
    private BerzaRepository berzaRepository;
    private UserService userService;

    private TransakcijaService transakcijaService;

    @Autowired
    public OrderService(OrderRepository orderRepository, FuturesUgovoriPodaciService futuresUgovoriPodaciService,
                        TransakcijaService transakcijaService, BerzaRepository berzaRepository, UserService userService){
        this.orderRepository = orderRepository;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
        this.transakcijaService = transakcijaService;
        this.berzaRepository = berzaRepository;
        this.userService = userService;
    }

    public Order getOrder(Long id) {
        return orderRepository.getById(id);
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

        executeOrder(id);

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

    public Order saveOrder(Long userAccount, Long hartijaOdVrednostiId, HartijaOdVrednostiType hartijaOdVrednostiType,
                           Integer kolicina, OrderAction orderAction, Double ukupnaCena, Double provizija,
                           OrderType orderType, boolean isAON, boolean isMargin, String oznakaHartije,
                           OrderStatus status, Double ask, Double bid){
        Order order = new Order();
        order.setUserId(userAccount);
        order.setHartijaOdVrednostiId(hartijaOdVrednostiId);
        order.setHartijaOdVrednosti(hartijaOdVrednostiType);
        order.setKolicina(kolicina);
        order.setOrderAction(orderAction);
        order.setUkupnaCena(ukupnaCena);
        order.setProvizija(provizija);
        order.setOrderType(orderType);
        order.setAON(isAON);
        order.setMargin(isMargin);
        order.setOznakaHartije(oznakaHartije);
        order.setLastModified(new Date());
        order.setOrderStatus(status);
        order.setAsk(ask);
        order.setBid(bid);

        return orderRepository.save(order);
    }

    public void finishOrder(Order order){
        order.setDone(true);
        order.setLastModified(new Date());
        orderRepository.save(order);
    }

    /**
     * OBRADA ORDERA
     */

    @Async
    @Transactional
    public OrderResponse executeOrder(Long orderId) {
        Order order = getOrder(orderId);

        if(!order.getOrderStatus().equals(OrderStatus.APPROVED))
            return new OrderResponse(MessageUtils.ORDER_REJECTED);

        executeTransaction(berzaId, order, order.getAsk(), order.getBid());
        order.setDone(true);
        return new OrderResponse(MessageUtils.ORDER_SUCCESSFUL);
    }

    public OrderResponse executeTransaction(Long berzaId, Order order, Double ask, Double bid){
        boolean flag = true;
        if(berzaId != -1){
            OrderStatusResponse orderStatus = getOrderStatus(berzaId);
            flag = orderStatus.isBerzaOtvorena();
        }
        if(order.isAON()){
            Transakcija transakcija;
            if(flag)
                transakcija = transactionOrder(order.getKolicina(), order, ask, bid);
            else
                transakcija = transactionOrderWithDelay(order.getKolicina(), order, ask, bid);
            if(berzaId != -1)
                addOrderToBerza(order, berzaId);
            return new OrderResponse("OK");
        }

        return executeMiniTransactions(berzaId, order, flag, ask, bid);
    }

    /**
     * Margin je povezan sa walletom korisnika koji ce biti detaljnije objasnjen u drugoj iteraciji
     * s obzirom na to, bice obradjen nakon nastavka specifikacije
     */
    public OrderResponse executeMiniTransactions(Long berzaId, Order order, boolean flag, Double ask, Double bid){
        Random random = new Random();
        int kolicina = order.getKolicina();
        int kolicinaZaTransakciju = random.nextInt(kolicina) + 1;

        if(order.getOrderAction().equals(OrderAction.BUY) && !canExecuteTransactionBuy(order, bid))
            return new OrderResponse(MessageUtils.ERROR);

        if(order.getOrderAction().equals(OrderAction.SELL) && !canExecuteTransactionSell(order, ask))
            return new OrderResponse(MessageUtils.ERROR);

        while(kolicina - kolicinaZaTransakciju > 0){
            //transakcija fixe delay
            if(flag)
                transactionOrder(kolicinaZaTransakciju, order, ask, bid);
            else
                transactionOrderWithDelay(kolicinaZaTransakciju, order, ask, bid);

            kolicina -= kolicinaZaTransakciju;
            kolicinaZaTransakciju = random.nextInt(kolicina) + 1;
        }

        finishOrder(order);

        return new OrderResponse("OK");
    }

    Transakcija transactionOrder(Integer transactionAmount, Order order, Double ask, Double bid){
        /**
         * MARKET_ORDER se izvrsava odmah, pa nema potrebe da cekamo
         */
        if(!order.getOrderType().equals(OrderType.MARKET_ORDER)) {
            try {
                int s = new Random().nextInt(((int)(0.24*60/order.getKolicina() * 1000)));
                Thread.sleep(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Transakcija transakcija = new Transakcija();
        transakcija.setOrder(order);
        transakcija.setCena(transactionAmount * order.getUkupnaCena());
        transakcija.setKolicina(transactionAmount);
        transakcija.setVremeTranskacije(new Date());
        return transakcijaService.saveTranskacija(transakcija);
    }

    /**
     * ukoliko je berza zatvorena prilikom ordera ili je u after-hours, korisnik ceka duze
     */
    public Transakcija transactionOrderWithDelay(Integer transactionAmount, Order order, Double ask, Double bid){
        try {
            //3s simulaciju 30 minuta
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return transactionOrder(transactionAmount, order, ask, bid);
    }

    public void addOrderToBerza(Order order, Long berzaId){
        Berza berza = berzaRepository.findBerzaById(berzaId);
        berza.getOrderi().add(order);
        berzaRepository.save(berza);
    }

    public boolean canExecuteTransactionBuy(Order order, Double bid){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getUkupnaCena() <= order.getLimitValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getUkupnaCena() <= order.getLimitValue() && order.getUkupnaCena() < bid){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
                break;
            case STOP_ORDER:
                if(order.getUkupnaCena() < bid)
                    return true;
                break;
            default:
                return true;
        }
        return false;
    }

    public boolean canExecuteTransactionSell(Order order, Double ask){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getUkupnaCena() > order.getLimitValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getUkupnaCena() > order.getLimitValue() && order.getUkupnaCena() > ask){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
            case STOP_ORDER:
                if(order.getUkupnaCena() > ask)
                    return true;
                break;
            default:
                return true;
        }
        return false;
    }

    public OrderStatusResponse getOrderStatus(Long id){
        Berza berza = berzaRepository.findBerzaById(id);
        Date date = new Date();

        String openTime = berza.getOpenTime();
        String closeTime = berza.getCloseTime();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        try {
            if(isOverlapping(dateFormat.parse(openTime), dateFormat.parse(closeTime), date))
                return new OrderStatusResponse(true, MessageUtils.ORDER_APPROVED);

            if(differenceInHours(dateFormat.parse(closeTime), date))
                return new OrderStatusResponse(false, MessageUtils.STOCK_AFTERHOURS);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new OrderStatusResponse(false, MessageUtils.STOCK_CLOSED);
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
