package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.FuturesUgovori;

public interface FuturesUgovoriRepository extends JpaRepository<FuturesUgovori, Long> {
}
