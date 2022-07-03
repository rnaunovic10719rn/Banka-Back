package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.CompanyRequest;
import rs.edu.raf.banka.racun.service.impl.CompanyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @InjectMocks
    CompanyService companyService;

    @Mock
    CompanyRepository companyRepository;

    @Test
    void testGetCompanyByPib(){
        Company cp = new Company();
        String pib = "pibMockito";

        given(companyRepository.findByPib(pib)).willReturn(cp);

        assertEquals(companyService.getCompanyByPib(pib), cp);
    }

    @Test
    void testGetCompanyByMaticniBroj(){
        Company cp = new Company();
        String mb = "maticniMockito";

        given(companyRepository.findByMaticniBroj(mb)).willReturn(cp);

        assertEquals(companyService.getCompanyByMaticniBroj(mb), cp);
    }

    @Test
    void testGetCompanyByNaziv(){
        List<Company> cps = new ArrayList<>();
        String naziv = "nazivMockito";

        for(int i = 0; i < 2; i++){
            cps.add(new Company());
        }

        given(companyRepository.findByNaziv(naziv)).willReturn(cps);

        assertEquals(companyService.getCompanyByNaziv(naziv), cps);
    }

    @Test
    void testGetCompanies(){
        List<Company> cps = new ArrayList<>();

        for(int i = 0; i < 2; i++){
            cps.add(new Company());
        }

        given(companyRepository.findAll()).willReturn(cps);

        assertEquals(companyService.getCompanies(), cps);
    }

    @Test
    void testGetCompanyById(){
        Company cp = new Company();
        Long id = 2L;

        given(companyRepository.findById(id)).willReturn(Optional.of(cp));

        assertEquals(companyService.getCompanyById(id), Optional.of(cp));
    }

    @Test
    void testCreateCompany(){
        Company cp = new Company();
        cp.setNaziv("nazivMockito");
        cp.setMaticniBroj("maticniMockito");
        cp.setPib("pibMockito");
        cp.setSifraDelatnosti("sifraMockito");
        cp.setDrzava("drzavaMockito");
        cp.setAdresa("adresaMockito");

        CompanyRequest cr = new CompanyRequest();
        cr.setNaziv("nazivMockito");
        cr.setMaticniBroj("maticniMockito");
        cr.setPib("pibMockito");
        cr.setSifraDelatnosti("sifraMockito");
        cr.setDrzava("drzavaMockito");
        cr.setAdresa("adresaMockito");

        given(companyRepository.save(cp)).willReturn(cp);

        assertEquals(companyService.createCompany(cr), cp);
    }

    @Test
    void testEditCompany(){
        Company cp = new Company();
        cp.setId(2L);
        cp.setNaziv("nazivMockito");
        cp.setMaticniBroj("maticniMockito");
        cp.setPib("pibMockito");
        cp.setSifraDelatnosti("sifraMockito");
        cp.setDrzava("drzavaMockito");
        cp.setAdresa("adresaMockito");

        CompanyRequest cr = new CompanyRequest();
        cr.setId(2L);
        cr.setNaziv("nazivMockito");
        cr.setMaticniBroj("maticniMockito");
        cr.setPib("pibMockito");
        cr.setSifraDelatnosti("sifraMockito");
        cr.setDrzava("drzavaMockito");
        cr.setAdresa("adresaMockito");

        given(companyRepository.findById(2L)).willReturn(Optional.of(cp));
        given(companyRepository.save(cp)).willReturn(cp);

        assertEquals(companyService.editCompany(cr), cp);
    }


}
