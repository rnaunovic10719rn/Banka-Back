package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.HartijaOdVrednosti;

import java.util.List;

public interface HartijaRepository extends JpaRepository<Forex, Long>, JpaSpecificationExecutor<Forex> {


    //TODO add union with options
    @Query("SELECT f FROM FuturesUgovori f WHERE f.settlementDate >= CURRENT_DATE ORDER BY f.settlementDate ASC")
    List<HartijaOdVrednosti> getAllNearSettlement();

}
