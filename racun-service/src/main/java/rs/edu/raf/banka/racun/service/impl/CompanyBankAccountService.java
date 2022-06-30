package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.exceptions.InvalidCompanyException;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyBankAccount;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyBankAccountRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.CompanyBankAccountRequest;
import rs.edu.raf.banka.racun.utils.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyBankAccountService {

    private final CompanyRepository companyRepository;
    private final CompanyBankAccountRepository companyBankAccountRepository;
    private final ValutaRepository valutaRepository;

    @Autowired
    public CompanyBankAccountService(CompanyRepository companyRepository, CompanyBankAccountRepository companyBankAccountRepository, ValutaRepository valutaRepository) {
        this.companyRepository = companyRepository;
        this.companyBankAccountRepository = companyBankAccountRepository;
        this.valutaRepository = valutaRepository;
    }

    private void validateBankAccountRequest(CompanyBankAccountRequest bankAccountRequest) {
        if(bankAccountRequest.getCompanyId() == null ||
                bankAccountRequest.getValutaId() == null ||
                StringUtils.emptyString(bankAccountRequest.getBrojRacuna()) ||
                StringUtils.emptyString(bankAccountRequest.getBanka())) {
            throw new InvalidCompanyException("Required parameter is missing");
        }
    }

    public CompanyBankAccount createBankAccount(CompanyBankAccountRequest bankAccountRequest) {
        if(bankAccountRequest.getId() != null) {
            throw new InvalidCompanyException("ID is not allowed when creating a new contact person");
        }
        validateBankAccountRequest(bankAccountRequest);

        Optional<Company> company = companyRepository.findById(bankAccountRequest.getCompanyId());
        if(company.isEmpty()) {
            throw new InvalidCompanyException(InvalidCompanyException.MESSAGE_DOES_NOT_EXIST);
        }
        Optional<Valuta> valuta = valutaRepository.findById(bankAccountRequest.getValutaId());
        if(valuta.isEmpty()) {
            throw new InvalidCompanyException("Provided currency does not exist");
        }

        CompanyBankAccount bankAccount = new CompanyBankAccount();
        bankAccount.setCompany(company.get());
        bankAccount.setValuta(valuta.get());
        bankAccount.setBrojRacuna(bankAccountRequest.getBrojRacuna());
        bankAccount.setBanka(bankAccountRequest.getBanka());
        bankAccount.setActive(true);

        return companyBankAccountRepository.save(bankAccount);
    }

    public CompanyBankAccount editBankAccount(CompanyBankAccountRequest bankAccountRequest) {
        if(bankAccountRequest.getId() == null) {
            throw new InvalidCompanyException("ID is required when editing an existing bank account");
        }
        validateBankAccountRequest(bankAccountRequest);

        Optional<Company> company = companyRepository.findById(bankAccountRequest.getCompanyId());
        if(company.isEmpty()) {
            throw new InvalidCompanyException(InvalidCompanyException.MESSAGE_DOES_NOT_EXIST);
        }
        Optional<Valuta> valuta = valutaRepository.findById(bankAccountRequest.getValutaId());
        if(valuta.isEmpty()) {
            throw new InvalidCompanyException("Provided currency does not exist");
        }

        Optional<CompanyBankAccount> bankAccountOptional = companyBankAccountRepository.findById(bankAccountRequest.getId());
        if(bankAccountOptional.isEmpty()) {
            throw new InvalidCompanyException("Provided bank account does not exist");
        }

        CompanyBankAccount bankAccount = bankAccountOptional.get();
        bankAccount.setCompany(company.get());
        bankAccount.setValuta(valuta.get());
        bankAccount.setBrojRacuna(bankAccountRequest.getBrojRacuna());
        bankAccount.setBanka(bankAccountRequest.getBanka());
        if(bankAccountRequest.getActive() != null) {
            bankAccount.setActive(bankAccountRequest.getActive());
        }

        return companyBankAccountRepository.save(bankAccount);
    }

    public Optional<CompanyBankAccount> getBankAccountById(Long id) {
        return companyBankAccountRepository.findById(id);
    }

    public List<CompanyBankAccount> getBankAccounts(Long companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        if(company.isEmpty()) {
            throw new InvalidCompanyException(InvalidCompanyException.MESSAGE_DOES_NOT_EXIST);
        }

        return companyBankAccountRepository.findByCompany(company.get());
    }

    public void deleteBankAccount(Long id) {
        Optional<CompanyBankAccount> bankAccount = companyBankAccountRepository.findById(id);
        if(bankAccount.isEmpty()) {
            throw new InvalidCompanyException("Provided bank account does not exist");
        }
        companyBankAccountRepository.delete(bankAccount.get());
    }

}
