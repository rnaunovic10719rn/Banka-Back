package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.berza.model.Akcije;

import java.util.List;

public interface AkcijeRepository extends JpaRepository<Akcije, Long>, JpaSpecificationExecutor<Akcije> {

    Akcije findAkcijeByOznakaHartije(String oznaka);

    Akcije findAkcijeById(Long id);

}
