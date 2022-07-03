package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyContactPerson;
import rs.edu.raf.banka.racun.repository.company.CompanyContactPersonRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.CompanyContactPersonRequest;
import rs.edu.raf.banka.racun.service.impl.CompanyContactPersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CompanyContactPersonServiceTest {
    @InjectMocks
    CompanyContactPersonService companyService;

    @Mock
    CompanyContactPersonRepository companyCPRepository;

    @Mock
    CompanyRepository companyRepository;

    @Test
    void testGetContactPersons(){
        Company cp = new Company();
        List<CompanyContactPerson> cps = new ArrayList<>();

        for(int i = 0; i < 2; i++){
            cps.add(new CompanyContactPerson());
        }

        given(companyRepository.findById(2L)).willReturn(Optional.of(cp));
        given(companyCPRepository.findByCompany(cp)).willReturn(cps);

        assertEquals(companyService.getContactPersons(2L), cps);
    }

    @Test
    void testGetContactPersonById(){
        CompanyContactPerson cp = new CompanyContactPerson();

        given(companyCPRepository.findById(2L)).willReturn(Optional.of(cp));

        assertEquals(companyService.getContactPersonById(2L), Optional.of(cp));
    }

    @Test
    void testCreateContactPerson(){
        Company mockCP = new Company();

        CompanyContactPerson cp = new CompanyContactPerson();
        cp.setCompany(mockCP);
        cp.setIme("mock");
        cp.setPrezime("mock");
        cp.setEmail("mock");
        cp.setBrojTelefona("mock");
        cp.setPozicija("mock");
        cp.setNapomena("mock");

        CompanyContactPersonRequest cpr = new CompanyContactPersonRequest();
        cpr.setCompanyId(2L);
        cpr.setIme("mock");
        cpr.setPrezime("mock");
        cpr.setEmail("mock");
        cpr.setBrojTelefona("mock");
        cpr.setPozicija("mock");
        cpr.setNapomena("mock");

        given(companyRepository.findById(cpr.getCompanyId())).willReturn(Optional.of(mockCP));
        given(companyCPRepository.save(cp)).willReturn(cp);

        assertEquals(companyService.createContactPerson(cpr), cp);
    }

    @Test
    void testEditContactPerson(){
        Company mockCP = new Company();

        CompanyContactPerson cp = new CompanyContactPerson();
        cp.setCompany(mockCP);
        cp.setIme("mock");
        cp.setPrezime("mock");
        cp.setEmail("mock");
        cp.setBrojTelefona("mock");
        cp.setPozicija("mock");
        cp.setNapomena("mock");

        CompanyContactPersonRequest cpr = new CompanyContactPersonRequest();
        cpr.setId(1L);
        cpr.setCompanyId(2L);
        cpr.setIme("mock");
        cpr.setPrezime("mock");
        cpr.setEmail("mock");
        cpr.setBrojTelefona("mock");
        cpr.setPozicija("mock");
        cpr.setNapomena("mock");

        given(companyRepository.findById(cpr.getCompanyId())).willReturn(Optional.of(mockCP));
        given(companyCPRepository.findById(1L)).willReturn(Optional.of(cp));
        given(companyCPRepository.save(cp)).willReturn(cp);

        assertEquals(companyService.editContactPerson(cpr), cp);
    }
}
