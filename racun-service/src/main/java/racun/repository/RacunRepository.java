package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import racun.model.Racun;

import java.util.UUID;


public interface RacunRepository extends JpaRepository<Racun, Long>{


    Racun findByBrojRacuna(UUID broj);

    Racun findByUsername(String username);
}
