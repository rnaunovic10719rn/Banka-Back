package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Valuta;


public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {


    SredstvaKapital findByRacunAndValuta(Racun racun, Valuta valuta);



}
