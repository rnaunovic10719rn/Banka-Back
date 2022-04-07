package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.Forex;

public interface ForexRepository extends JpaRepository<Forex, Long> {
}
