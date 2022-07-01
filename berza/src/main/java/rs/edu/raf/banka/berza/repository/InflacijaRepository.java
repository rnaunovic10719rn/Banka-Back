package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.berza.model.IstorijaInflacije;
import rs.edu.raf.banka.berza.model.Valuta;

import java.util.Optional;

public interface InflacijaRepository extends JpaRepository<IstorijaInflacije, Long> {

    Optional<IstorijaInflacije> findByValutaAndYear(Valuta valuta, String year);

}
