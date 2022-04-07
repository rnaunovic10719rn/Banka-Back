package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.Akcije;

public interface AkcijeRepository extends JpaRepository<Akcije, Long> {
}
