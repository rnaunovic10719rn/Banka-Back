package rs.edu.raf.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.Valuta;

public interface ForexRepository extends JpaRepository<Forex, Long>, JpaSpecificationExecutor<Forex> {

    Forex findForexById(Long id);

    Forex findForexByOznakaHartije(String oznakaHartije);

    Forex findForexByBaseCurrencyAndQuoteCurrency(Valuta base_currency, Valuta quote_currency);

}
