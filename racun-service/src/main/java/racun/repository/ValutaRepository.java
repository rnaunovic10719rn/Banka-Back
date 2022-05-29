package racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import racun.model.Valuta;

public interface ValutaRepository extends JpaRepository<Valuta,Long> {

    Valuta findValutaByOznakaValute(String oznaka);
}
