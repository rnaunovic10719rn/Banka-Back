package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {


}
