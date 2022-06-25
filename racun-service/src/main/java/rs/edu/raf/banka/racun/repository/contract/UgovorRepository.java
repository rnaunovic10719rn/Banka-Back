package rs.edu.raf.banka.racun.repository.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.model.company.Company;

import java.util.List;

public interface UgovorRepository extends JpaRepository<Ugovor, Long> {
    List<Ugovor> findAllByCompany(Company company);
    List<Ugovor> findAllByCompanyAndUserId(Company company, Long userId);
    List<Ugovor> findAllByCompanyAndStatus(Company company, UgovorStatus status);
    List<Ugovor> findAllByCompanyAndStatusAndUserId(Company company, UgovorStatus status, Long userId);
    List<Ugovor> findAllByStatus(UgovorStatus status);
    List<Ugovor> findAllByUserId(Long userId);
    List<Ugovor> findAllByStatusAndUserId(UgovorStatus status, Long userId);
    Ugovor findByDelovodniBroj(String delovodniBroj);
}
