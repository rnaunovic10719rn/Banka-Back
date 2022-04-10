package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.edu.raf.banka.berza.model.Transakcija;

import java.util.Date;
import java.util.List;

public interface TranskacijaRepository extends JpaRepository<Transakcija, Long> {

    @Query("SELECT t.cena FROM Transakcija t WHERE t.order.orderAction = rs.edu.raf.banka.berza.enums.OrderAction.BUY " +
            "AND t.vremeTranskacije = :datum ORDER BY ABS(t.cena - :cena) DESC")
    List<Double> findCeneTransakcijaBuy(@Param("datum") Date datum, @Param("cena") Double cena);

    @Query("SELECT t.cena FROM Transakcija t WHERE t.order.orderAction = rs.edu.raf.banka.berza.enums.OrderAction.SELL " +
            "AND t.vremeTranskacije = :datum ORDER BY ABS(t.cena - :cena) DESC")
    List<Double> findCeneTransakcijaSell(Date datum, @Param("cena") Double cena);
}
