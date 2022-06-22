package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.Ugovor;

import java.util.List;

public interface TransakcionaStavkaRepository extends JpaRepository<TransakcionaStavka, Long> {

}
