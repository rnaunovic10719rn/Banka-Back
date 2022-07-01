package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rs.edu.raf.banka.berza.model.Valuta;

import java.util.Optional;

public interface ValutaRepository extends JpaRepository<Valuta, Long>, JpaSpecificationExecutor<Valuta>  {

    Valuta findByOznakaValute(String valuta);
    Optional<Valuta> getValutaByNazivValute(String nazivValute);

}
