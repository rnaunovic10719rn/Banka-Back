package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.Berza;

public interface BerzaRepository extends JpaRepository<Berza, Long> {

    Berza findBerzaById(Long id);
}
