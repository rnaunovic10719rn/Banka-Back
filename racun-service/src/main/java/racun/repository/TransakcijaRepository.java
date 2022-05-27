package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import racun.model.Transakcija;

import java.util.List;

public interface TransakcijaRepository extends JpaRepository<Transakcija,Long> {

    @Query("SELECT T FROM Transakcija T WHERE T.username=:username")
    List<Transakcija> findByUsername(String username);
}
