package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.OrderDto;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.model.UserAccount;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, AkcijeRepository akcijeRepository,
                        ForexRepository forexRepository, FuturesUgovoriRepository futuresUgovoriRepository){
        this.orderRepository = orderRepository;
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
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

        return this.orderRepository.save(order);
    }

}
