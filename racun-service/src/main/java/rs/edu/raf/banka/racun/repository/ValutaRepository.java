package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.model.Valuta;

public interface ValutaRepository extends JpaRepository<Valuta,Long> {

    Valuta findValutaByKodValute(String kodValute);
}
