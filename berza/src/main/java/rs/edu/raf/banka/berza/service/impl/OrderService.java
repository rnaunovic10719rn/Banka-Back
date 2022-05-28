package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Autowired
    public OrderService(OrderRepository orderRepository, FuturesUgovoriPodaciService futuresUgovoriPodaciService){
        this.orderRepository = orderRepository;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
    }

    public List<Order> getOrders(String status, Boolean done) {
        if(status.length() == 0 && done == null)
            return orderRepository.findAll();
        return orderRepository.findOrderByOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
    }

    public ApproveRejectOrderResponse approveOrder(Long id){
        //provera supervizora
        Order order = orderRepository.getById(id);

        if (order.getHartijaOdVrednosti() == HartijaOdVrednostiType.FUTURES_UGOVOR &&
                !futuresUgovoriPodaciService.isRelevant(order.getHartijaOdVrednostiId())) {
            return rejectOrder(id);
        }

        order.setOrderStatus(OrderStatus.APPROVED);
        order.setLastModified(new Date());
        orderRepository.save(order);

        return new ApproveRejectOrderResponse("Order Approved");
    }

    public ApproveRejectOrderResponse rejectOrder(Long id){
        //provera supervizora
        Order order = orderRepository.getById(id);
        order.setOrderStatus(OrderStatus.REJECTED);
        order.setLastModified(new Date());
        orderRepository.save(order);

        return new ApproveRejectOrderResponse("Order Rejected");
    }

    public Order saveOrder(Long userAccount, Long hartijaOdVrednostiId, HartijaOdVrednostiType hartijaOdVrednostiType,
                           Integer kolicina, OrderAction orderAction, Double ukupnaCena, Double provizija,
                           OrderType orderType, boolean isAON, boolean isMargin, String oznakaHartije){
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

        //
        order.setOrderStatus(OrderStatus.ON_HOLD);

        return this.orderRepository.save(order);
    }

    public void finishOrder(Order order){
        order.setDone(true);
        order.setLastModified(new Date());
        orderRepository.save(order);
    }

}
