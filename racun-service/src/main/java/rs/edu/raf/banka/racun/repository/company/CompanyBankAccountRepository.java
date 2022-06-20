package rs.edu.raf.banka.racun.repository.company;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyBankAccount;

import java.util.List;

public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Long> {

    List<CompanyBankAccount> findByCompany(Company company);

}
