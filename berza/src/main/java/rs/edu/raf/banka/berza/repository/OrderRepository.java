package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.model.Order;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findOrderByOrderStatus(OrderStatus orderStatus);

    List<Order> findOrderByOrderStatusAndUserId(OrderStatus orderStatus, Long userId);

    List<Order> findOrdersByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE (:orderStatus is NULL OR o.orderStatus = :orderStatus) AND " +
            "(:done IS NULL OR o.done = :done)")
    List<Order> findOrders(OrderStatus orderStatus, Boolean done);
}
