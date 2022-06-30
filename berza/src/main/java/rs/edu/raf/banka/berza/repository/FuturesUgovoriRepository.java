package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rs.edu.raf.banka.berza.model.FuturesUgovori;

import java.util.Date;

public interface FuturesUgovoriRepository extends JpaRepository<FuturesUgovori, Long>, JpaSpecificationExecutor<FuturesUgovori> {

    FuturesUgovori findFuturesById(Long id);

    FuturesUgovori findFuturesUgovoriByOznakaHartije(String oznakaHartije);

    FuturesUgovori findFuturesUgovoriByIdAndSettlementDateAfter(Long id, Date date);

}
