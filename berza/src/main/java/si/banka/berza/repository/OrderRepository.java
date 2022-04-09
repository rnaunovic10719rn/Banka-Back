package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.Order;


public interface OrderRepository extends JpaRepository<Order, Long> {


}
