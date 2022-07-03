package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyBankAccount;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyBankAccountRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.CompanyBankAccountRequest;
import rs.edu.raf.banka.racun.service.impl.CompanyBankAccountService;
import rs.edu.raf.banka.racun.service.impl.CompanyContactPersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CompanyBankAccountServiceTest {
    @InjectMocks
    CompanyBankAccountService companyService;

    @Mock
    CompanyBankAccountRepository companyBankAccountRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    ValutaRepository valutaRepository;

    @Test
    void testGetBankAccounts(){
        Company cp = new Company();
        List<CompanyBankAccount> cbas = new ArrayList<>();

        for(int i = 0; i < 2; i++){
            cbas.add(new CompanyBankAccount());
        }

        given(companyRepository.findById(2L)).willReturn(Optional.of(cp));
        given(companyBankAccountRepository.findByCompany(cp)).willReturn(cbas);

        assertEquals(companyService.getBankAccounts(2L), cbas);

    }

    @Test
    void testGetBankAccountById(){
        CompanyBankAccount cb = new CompanyBankAccount();

        given(companyBankAccountRepository.findById(2L)).willReturn(Optional.of(cb));

        assertEquals(companyService.getBankAccountById(2L), Optional.of(cb));
    }

    @Test
    void testCreateBankAccount(){
        Company cp = new Company();

        Valuta val = new Valuta();

        CompanyBankAccount cb = new CompanyBankAccount();
        cb.setCompany(cp);
        cb.setValuta(val);
        cb.setBanka("bankaMockito");
        cb.setBrojRacuna("123");
        cb.setActive(true);

        CompanyBankAccountRequest cbar = new CompanyBankAccountRequest();
        cbar.setCompanyId(2L);
        cbar.setValutaId(3L);
        cbar.setBrojRacuna("123");
        cbar.setBanka("bankaMockito");
        cbar.setActive(true);

        given(companyRepository.findById(2L)).willReturn(Optional.of(cp));
        given(valutaRepository.findById(3L)).willReturn(Optional.of(val));
        given(companyBankAccountRepository.save(cb)).willReturn(cb);

        assertEquals(companyService.createBankAccount(cbar), cb);
    }

    @Test
    void testEditBankAccount(){
        Company cp = new Company();

        Valuta val = new Valuta();

        CompanyBankAccount cb = new CompanyBankAccount();
        cb.setCompany(cp);
        cb.setValuta(val);
        cb.setBanka("bankaMockito");
        cb.setBrojRacuna("123");
        cb.setActive(true);

        CompanyBankAccountRequest cbar = new CompanyBankAccountRequest();
        cbar.setId(1L);
        cbar.setCompanyId(2L);
        cbar.setValutaId(3L);
        cbar.setBrojRacuna("123");
        cbar.setBanka("bankaMockito");
        cbar.setActive(true);

        given(companyBankAccountRepository.findById(1L)).willReturn(Optional.of(cb));
        given(companyRepository.findById(2L)).willReturn(Optional.of(cp));
        given(valutaRepository.findById(3L)).willReturn(Optional.of(val));
        given(companyBankAccountRepository.save(cb)).willReturn(cb);

        assertEquals(companyService.editBankAccount(cbar), cb);
    }
}
