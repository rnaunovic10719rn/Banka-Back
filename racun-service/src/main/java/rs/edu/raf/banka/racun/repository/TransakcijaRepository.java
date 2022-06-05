package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.model.Transakcija;

import java.util.List;

public interface TransakcijaRepository extends JpaRepository<Transakcija,Long> {

    @Query("SELECT T FROM Transakcija T WHERE T.username=:username")
    List<Transakcija> findByUsername(String username);

    @Query("SELECT SUM(rezervisano) - SUM(rezervisanoKoristi) FROM Transakcija WHERE orderId = :orderId")
    Double getRezervisanoForOrder(Long orderId);
}
