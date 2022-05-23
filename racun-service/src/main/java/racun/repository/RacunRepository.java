package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import racun.model.Racun;


public interface RacunRepository extends JpaRepository<Racun, Long>{

    @Query("SELECT R FROM Racun R WHERE R.brojRacuna=:broj")
    Racun findByBroj(String broj);
}
