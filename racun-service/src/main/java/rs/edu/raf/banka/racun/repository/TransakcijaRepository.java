package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;

import java.util.Date;
import java.util.List;

public interface TransakcijaRepository extends JpaRepository<Transakcija,Long> {

    @Query("SELECT T FROM Transakcija T WHERE T.username=:username ORDER BY T.datumVreme DESC")
    List<Transakcija> findByUsername(String username);


    @Query("SELECT T FROM Transakcija T WHERE T.username=:username AND T.datumVreme >=:odFilter AND T.datumVreme <=:doFilter ORDER BY T.datumVreme DESC")
    List<Transakcija> findByUsername(String username, Date odFilter, Date doFilter);

    @Query("SELECT T FROM Transakcija T WHERE T.username=:username AND T.valuta.kodValute=:valutaKod  ORDER BY T.datumVreme DESC")
    List<Transakcija> findByUsername(String username, String valutaKod);

    @Query("SELECT T FROM Transakcija T WHERE T.username=:username AND T.valuta.kodValute=:valutaKod AND T.datumVreme >=:odFilter AND T.datumVreme <=:doFilter ORDER BY T.datumVreme DESC")
    List<Transakcija> findByUsername(String username, String valutaKod, Date odFilter, Date doFilter);

    @Query("SELECT SUM(rezervisano) - SUM(rezervisanoKoristi) FROM Transakcija WHERE orderId = :orderId")
    Double getRezervisanoForOrder(Long orderId);

}
