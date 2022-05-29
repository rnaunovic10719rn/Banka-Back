package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import racun.model.Racun;
import racun.model.SredstvaKapital;

import java.util.UUID;

public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {

    @Query("SELECT SK FROM SredstvaKapital SK LEFT JOIN Racun R ON R.id=SK.racun.id WHERE R.username=:username")
    SredstvaKapital findByUser(String username);

    SredstvaKapital findByRacun(Racun racun);

}
