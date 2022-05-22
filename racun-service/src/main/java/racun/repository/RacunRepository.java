package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import racun.model.Racun;

import java.util.List;

public interface RacunRepository extends JpaRepository<Racun, Long>{


}
