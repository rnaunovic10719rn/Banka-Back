package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Valuta;

import java.util.List;


public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {

    List<SredstvaKapital> findAllByRacun(Racun racun);

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.valuta = :valuta AND s.kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.NOVAC")
    SredstvaKapital findByRacunAndValuta(Racun racun, Valuta valuta);

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.haritjeOdVrednostiID = :hartijaId AND s.kapitalType = :hartijaType")
    SredstvaKapital findByRacunAndHaritja(Racun racun, KapitalType hartijaType, Long hartijaId);
    List<SredstvaKapital> findAll();

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.valuta = :valuta AND s.kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.NOVAC")
    List<SredstvaKapital> findAllByRacunAndValuta(Racun racun, Valuta valuta);

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.valuta = :valuta AND s.haritjeOdVrednostiID = :hartijaId AND s.kapitalType = :hartijaType")
    List<SredstvaKapital> findAllByRacunAndValutaAndHaritja(Racun racun, Valuta valuta, KapitalType hartijaType, Long hartijaId);

    SredstvaKapital findByRacunAndKapitalType(Racun racun, KapitalType kapitalType);

    List<SredstvaKapital> findAllByRacunAndKapitalType(Racun racun, KapitalType kapitalType);

}
