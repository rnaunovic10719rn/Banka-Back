package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.dto.SredstvaKapitalDto;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Valuta;

import java.util.List;


public interface SredstvaKapitalRepository extends JpaRepository<SredstvaKapital,Long> {


    SredstvaKapital findByRacunAndValuta(Racun racun, Valuta valuta);

    List<SredstvaKapital> findAll();


}
