package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;

import java.util.Date;
import java.util.List;

public interface MarginTransakcijaRepository extends JpaRepository<MarginTransakcija, Long> {

    @Query("SELECT T FROM MarginTransakcija T ORDER BY T.datumVreme DESC")
    List<MarginTransakcija> getAll();
    @Query("SELECT T FROM MarginTransakcija T WHERE T.username=:username ORDER BY T.datumVreme DESC")
    List<MarginTransakcija> findByUsername(String username);

    @Query("SELECT T FROM MarginTransakcija T WHERE T.datumVreme >= :odFilter AND T.datumVreme <= :doFilter ORDER BY T.datumVreme DESC")
    List<MarginTransakcija> getAll(Date odFilter, Date doFilter);

    @Query("SELECT T FROM MarginTransakcija T WHERE T.username=:username AND T.datumVreme >=:odFilter AND T.datumVreme <=:doFilter ORDER BY T.datumVreme DESC")
    List<MarginTransakcija> findByUsername(String username, Date odFilter, Date doFilter);

    @Query("SELECT SUM(unitPrice*kolicina) FROM MarginTransakcija WHERE haritjeOdVrednostiID = :hartijaId AND kapitalType = :kapitalType")
    Double getKupljenoZa(Long hartijaId, KapitalType kapitalType);

    List<MarginTransakcija> findByHaritjeOdVrednostiIDAndKapitalTypeAndRacun(Long hartijeOdVrednostiID, KapitalType kapitalType, Racun racun);

}
