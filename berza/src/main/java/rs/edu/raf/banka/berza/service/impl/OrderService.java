package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.model.UserAccount;
import rs.edu.raf.banka.berza.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    public Order saveOrder(UserAccount userAccount, Long hartijaOdVrednostiId, HartijaOdVrednostiType hartijaOdVrednostiType,
                           Integer kolicina, OrderAction orderAction, Double ukupnaCena, Double provizija,
                           OrderType orderType, boolean isAON, boolean isMargin){
        Order order = new Order();
        order.setUserAccount(userAccount);
        order.setHartijaOdVrednostiId(hartijaOdVrednostiId);
        order.setHartijaOdVrednosti(hartijaOdVrednostiType);
        order.setKolicina(kolicina);
        order.setOrderAction(orderAction);
        order.setUkupnaCena(ukupnaCena);
        order.setProvizija(provizija);
        order.setOrderType(orderType);
        order.setAON(isAON);
        order.setMargin(isMargin);

        return this.orderRepository.save(order);
    }
}
