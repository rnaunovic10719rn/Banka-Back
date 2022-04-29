package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.berza.model.IstorijaInflacije;

public interface InflacijaRepository extends JpaRepository<IstorijaInflacije, Long> {

}
