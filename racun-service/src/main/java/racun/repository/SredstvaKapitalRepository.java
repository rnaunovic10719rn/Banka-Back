package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import racun.model.Racun;
import racun.model.SredstvaKapital;


public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {


    SredstvaKapital findByRacun(Racun racun);

}
