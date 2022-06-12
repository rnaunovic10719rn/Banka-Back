package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;

import java.util.UUID;


public interface RacunRepository extends JpaRepository<Racun, Long>{

    Racun findRacunByTipRacuna(RacunType racunType);

    Racun findByBrojRacuna(UUID broj);

}
