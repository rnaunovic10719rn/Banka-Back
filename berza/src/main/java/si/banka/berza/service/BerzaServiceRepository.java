package si.banka.berza.service;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.berza.model.Berza;

public interface BerzaServiceRepository extends JpaRepository<Berza, Long> {

    Berza findById_berze(Long id);
}
