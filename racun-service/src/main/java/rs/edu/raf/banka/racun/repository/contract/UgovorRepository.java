package rs.edu.raf.banka.racun.repository.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.model.company.Company;

import java.util.List;

public interface UgovorRepository extends JpaRepository<Ugovor, Long> {
    List<Ugovor> findAllByCompany(Company company);
    List<Ugovor> findAllByCompanyAndAgentId(Company company, Long agentId);
    List<Ugovor> findAllByCompanyAndStatus(Company company, UgovorStatus status);
    List<Ugovor> findAllByCompanyAndStatusAndAgentId(Company company, UgovorStatus status, Long agentId);
    List<Ugovor> findAllByStatus(UgovorStatus status);
    List<Ugovor> findAllByAgentId(Long agentId);
    List<Ugovor> findAllByStatusAndAgentId(UgovorStatus status, Long agentId);
    Ugovor findByDelodavniBroj(String delodavniBroj);
}
