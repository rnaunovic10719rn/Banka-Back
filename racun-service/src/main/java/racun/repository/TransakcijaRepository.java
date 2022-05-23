package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import racun.model.Transakcija;

public interface TransakcijaRepository extends JpaRepository<Transakcija,Long> {


}
