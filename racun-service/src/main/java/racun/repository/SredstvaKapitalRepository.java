package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import racun.model.Racun;
import racun.model.SredstvaKapital;
import racun.model.Valuta;


public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {


    SredstvaKapital findByRacunAndValuta(Racun racun, Valuta valuta);



}
