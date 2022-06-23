package rs.edu.raf.banka.racun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.Ugovor;
import rs.edu.raf.banka.racun.model.company.Company;

import java.util.List;

public interface UgovorRepository extends JpaRepository<Ugovor, Long> {
    List<Ugovor> findAllByCompany(Company company);
    List<Ugovor> findAllByCompanyAndStatus(Company company, UgovorStatus status);
    List<Ugovor> findAllByStatus(UgovorStatus status);
    Ugovor findByDelodavniBroj(String delodavniBroj);
}
