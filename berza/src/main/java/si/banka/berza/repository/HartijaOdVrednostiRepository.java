package si.banka.berza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import si.banka.berza.model.HartijaOdVrednosti;

@NoRepositoryBean
public interface HartijaOdVrednostiRepository<T extends HartijaOdVrednosti> extends JpaRepository<T, Long> {

}
