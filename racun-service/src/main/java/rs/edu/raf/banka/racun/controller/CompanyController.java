package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyBankAccount;
import rs.edu.raf.banka.racun.model.company.CompanyContactPerson;
import rs.edu.raf.banka.racun.requests.CompanyBankAccountRequest;
import rs.edu.raf.banka.racun.requests.CompanyContactPersonRequest;
import rs.edu.raf.banka.racun.requests.CompanyRequest;
import rs.edu.raf.banka.racun.service.impl.CompanyBankAccountService;
import rs.edu.raf.banka.racun.service.impl.CompanyContactPersonService;
import rs.edu.raf.banka.racun.service.impl.CompanyService;

import java.util.Optional;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyContactPersonService contactPersonService;
    private final CompanyBankAccountService bankAccountService;

    @Autowired
    public CompanyController(CompanyService companyService, CompanyContactPersonService contactPersonService, CompanyBankAccountService bankAccountService) {
        this.companyService = companyService;
        this.contactPersonService = contactPersonService;
        this.bankAccountService = bankAccountService;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCompanies(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(companyService.getCompanies());
    }

    @GetMapping(value = "/{id}/{naziv}/{maticniBroj}/{pib}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCompany(@RequestHeader("Authorization") String token, @PathVariable(required = false) Long id,
                                        @PathVariable(required = false) String naziv, @PathVariable(required = false) String maticniBroj,
                                        @PathVariable(required = false) String pib) {
        if (id != null) {
            Optional<Company> company = companyService.getCompanyById(id);
            if (company.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(company.get());
        } else if (naziv != null) {
            Company company = companyService.getCompanyByNaziv(naziv);
            return ResponseEntity.ok(company);
        } else if (maticniBroj != null) {
            Company company = companyService.getCompanyByMaticniBroj(maticniBroj);
            return ResponseEntity.ok(company);
        } else if (pib != null) {
            Company company = companyService.getCompanyByPib(pib);
            return ResponseEntity.ok(company);
        }
        return ResponseEntity.badRequest().body("bad request!");
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCompany(@RequestHeader("Authorization") String token, @RequestBody CompanyRequest companyRequest) {
        return ResponseEntity.ok(companyService.createCompany(companyRequest));
    }

    @PostMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editCompany(@RequestHeader("Authorization") String token, @RequestBody CompanyRequest companyRequest) {
        return ResponseEntity.ok(companyService.editCompany(companyRequest));
    }

    /**
     * Contact persons
     */
    @GetMapping(value = "/contact/{companyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getContactPersons(@RequestHeader("Authorization") String token, @PathVariable Long companyId) {
        return ResponseEntity.ok(contactPersonService.getContactPersons(companyId));
    }

    @GetMapping(value = "/contact/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getContactPerson(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<CompanyContactPerson> contactPerson = contactPersonService.getContactPersonById(id);
        if(contactPerson.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contactPerson.get());
    }

    @PostMapping(value = "/contact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createContactPerson(@RequestHeader("Authorization") String token, @RequestBody CompanyContactPersonRequest contactPersonRequest) {
        return ResponseEntity.ok(contactPersonService.createContactPerson(contactPersonRequest));
    }

    @PostMapping(value = "/contact/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editContactPerson(@RequestHeader("Authorization") String token, @RequestBody CompanyContactPersonRequest contactPersonRequest) {
        return ResponseEntity.ok(contactPersonService.editContactPerson(contactPersonRequest));
    }

    @DeleteMapping(value = "/contact/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteContactPerson(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        contactPersonService.deleteContactPerson(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Bank accounts
     */
    @GetMapping(value = "/bankaccount/{companyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBankAccounts(@RequestHeader("Authorization") String token, @PathVariable Long companyId) {
        return ResponseEntity.ok(bankAccountService.getBankAccounts(companyId));
    }

    @GetMapping(value = "/bankaccount/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBankAccount(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<CompanyBankAccount> bankAccount = bankAccountService.getBankAccountById(id);
        if(bankAccount.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bankAccount.get());
    }

    @PostMapping(value = "/bankaccount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBankAccount(@RequestHeader("Authorization") String token, @RequestBody CompanyBankAccountRequest bankAccountRequest) {
        return ResponseEntity.ok(bankAccountService.createBankAccount(bankAccountRequest));
    }

    @PostMapping(value = "/bankaccount/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editContactPerson(@RequestHeader("Authorization") String token, @RequestBody CompanyBankAccountRequest bankAccountRequest) {
        return ResponseEntity.ok(bankAccountService.editBankAccount(bankAccountRequest));
    }

}
