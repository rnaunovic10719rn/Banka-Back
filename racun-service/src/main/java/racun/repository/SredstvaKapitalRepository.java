package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import racun.model.SredstvaKapital;

public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {

    @Query("SELECT SK FROM SredstvaKapital SK LEFT JOIN Racun R ON R.id=SK.racun.id WHERE R.userID=:userID")
    SredstvaKapital findByUser(Long userID);

}
