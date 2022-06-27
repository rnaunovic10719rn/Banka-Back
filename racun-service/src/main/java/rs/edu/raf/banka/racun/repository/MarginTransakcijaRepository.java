package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;

public interface MarginTransakcijaRepository extends JpaRepository<MarginTransakcija, Long> {

}
