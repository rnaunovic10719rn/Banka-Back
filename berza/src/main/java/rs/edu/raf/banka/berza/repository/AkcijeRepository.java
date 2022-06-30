package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rs.edu.raf.banka.berza.model.Akcije;

public interface AkcijeRepository extends JpaRepository<Akcije, Long>, JpaSpecificationExecutor<Akcije> {

    Akcije findAkcijeByOznakaHartije(String oznaka);

    Akcije findAkcijeById(Long id);

}
