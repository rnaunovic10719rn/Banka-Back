package rs.edu.raf.banka.racun.repository.company;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.model.company.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}
