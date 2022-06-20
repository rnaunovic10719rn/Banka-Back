package rs.edu.raf.banka.racun.repository.company;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyContactPerson;

import java.util.List;

public interface CompanyContactPersonRepository extends JpaRepository<CompanyContactPerson, Long> {

    List<CompanyContactPerson> findByCompany(Company company);

}
