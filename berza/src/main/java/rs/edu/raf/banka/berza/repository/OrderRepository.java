package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.model.Order;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findOrderByOrderStatusAndDone(OrderStatus orderStatus, Boolean done);

    List<Order> findOrderByUserIdAndDoneAndOrderStatus(Long userId, Boolean done, OrderStatus orderStatus);

    List<Order> findOrderByUserId(Long userId);

    List<Order> findOrderByDoneFalse();

}
