package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.berza.model.Order;


public interface OrderRepository extends JpaRepository<Order, Long> {


}
