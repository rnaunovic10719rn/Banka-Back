package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Valuta;

import java.util.List;


public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {


    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.valuta = :valuta AND s.kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.NOVAC")
    SredstvaKapital findByRacunAndValuta(Racun racun, Valuta valuta);

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.haritjeOdVrednostiID = :hartijaId AND s.kapitalType <> rs.edu.raf.banka.racun.enums.KapitalType.NOVAC")
    SredstvaKapital findByRacunAndAndHaritjeOdVrednostiID(Racun racun, Long hartijaId);

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.valuta = :valuta AND s.kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.NOVAC")
    List<SredstvaKapital> findAllByRacunAndValuta(Racun racun, Valuta valuta);

    @Query("SELECT s FROM SredstvaKapital s WHERE s.racun = :racun AND s.haritjeOdVrednostiID = :hartijaId AND s.kapitalType <> rs.edu.raf.banka.racun.enums.KapitalType.NOVAC")
    List<SredstvaKapital> findAllByRacunAndAndHaritjeOdVrednostiID(Racun racun, Long hartijaId);

}
