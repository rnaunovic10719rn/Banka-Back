package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;
    private BerzaService berzaService;

    @Autowired
    public OrderService(OrderRepository orderRepository, FuturesUgovoriPodaciService futuresUgovoriPodaciService,
                        BerzaService berzaService){
        this.orderRepository = orderRepository;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
        this.berzaService = berzaService;
    }

    public Order getOrder(Long id) {
        return orderRepository.getById(id);
    }

    public List<Order> getOrders(String status, Boolean done) {
        if(status.length() == 0 && done == null)
            return orderRepository.findAll();
        return orderRepository.findOrderByOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
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

    //    order.setOrderManagerId();
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setLastModified(new Date());
        orderRepository.save(order);

        berzaService.executeOrder(id);

        return new ApproveRejectOrderResponse(MessageUtils.ORDER_APPROVED);
    }

    public ApproveRejectOrderResponse rejectOrder(String userRole, Long id){
        if(!isSupervisor(userRole)) {
            return new ApproveRejectOrderResponse(MessageUtils.REQUIRED_PERMISSION);
        }

        Order order = orderRepository.getById(id);
        //    order.setOrderManagerId();
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
        return this.orderRepository.save(order);
    }

    public void finishOrder(Order order){
        order.setDone(true);
        order.setLastModified(new Date());
        orderRepository.save(order);
    }

    private boolean isSupervisor(String userRole) {
        return UserRole.ROLE_SUPERVISOR.equals(UserRole.valueOf(userRole));
    }
}
