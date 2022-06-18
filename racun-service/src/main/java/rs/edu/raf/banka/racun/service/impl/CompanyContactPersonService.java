package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.exceptions.InvalidCompanyException;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyContactPerson;
import rs.edu.raf.banka.racun.repository.company.CompanyContactPersonRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.CompanyContactPersonRequest;
import rs.edu.raf.banka.racun.utils.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyContactPersonService {

    private final CompanyRepository companyRepository;
    private final CompanyContactPersonRepository companyContactPersonRepository;

    @Autowired
    public CompanyContactPersonService(CompanyRepository companyRepository, CompanyContactPersonRepository companyContactPersonRepository) {
        this.companyRepository = companyRepository;
        this.companyContactPersonRepository = companyContactPersonRepository;
    }

    private void validateContactPersonRequest(CompanyContactPersonRequest contactPersonRequest) {
        if(contactPersonRequest.getCompanyId() == null ||
                StringUtils.emptyString(contactPersonRequest.getIme()) ||
                StringUtils.emptyString(contactPersonRequest.getPrezime()) ||
                StringUtils.emptyString(contactPersonRequest.getEmail()) ||
                StringUtils.emptyString(contactPersonRequest.getBrojTelefona())) {
            throw new InvalidCompanyException("Required parameter is missing");
        }
    }

    public CompanyContactPerson createContactPerson(CompanyContactPersonRequest contactPersonRequest) {
        if(contactPersonRequest.getId() != null) {
            throw new InvalidCompanyException("ID is not allowed when creating a new contact person");
        }
        validateContactPersonRequest(contactPersonRequest);

        Optional<Company> company = companyRepository.findById(contactPersonRequest.getCompanyId());
        if(company.isEmpty()) {
            throw new InvalidCompanyException(InvalidCompanyException.MESSAGE_DOES_NOT_EXIST);
        }

        CompanyContactPerson contactPerson = new CompanyContactPerson();
        contactPerson.setCompany(company.get());
        contactPerson.setIme(contactPersonRequest.getIme());
        contactPerson.setPrezime(contactPersonRequest.getPrezime());
        contactPerson.setEmail(contactPersonRequest.getEmail());
        contactPerson.setBrojTelefona(contactPersonRequest.getBrojTelefona());
        contactPerson.setPozicija(contactPersonRequest.getPozicija());
        contactPerson.setNapomena(contactPersonRequest.getNapomena());

        return companyContactPersonRepository.save(contactPerson);
    }

    public CompanyContactPerson editContactPerson(CompanyContactPersonRequest contactPersonRequest) {
        if(contactPersonRequest.getId() == null) {
            throw new InvalidCompanyException("ID is required when editing an existing contact person");
        }
        validateContactPersonRequest(contactPersonRequest);

        Optional<Company> company = companyRepository.findById(contactPersonRequest.getCompanyId());
        if(company.isEmpty()) {
            throw new InvalidCompanyException(InvalidCompanyException.MESSAGE_DOES_NOT_EXIST);
        }

        Optional<CompanyContactPerson> contactPersonOptional = companyContactPersonRepository.findById(contactPersonRequest.getId());
        if(contactPersonOptional.isEmpty()) {
            throw new InvalidCompanyException("Provided contact person does not exist");
        }

        CompanyContactPerson contactPerson = contactPersonOptional.get();
        contactPerson.setCompany(company.get());
        contactPerson.setIme(contactPersonRequest.getIme());
        contactPerson.setPrezime(contactPersonRequest.getPrezime());
        contactPerson.setEmail(contactPersonRequest.getEmail());
        contactPerson.setBrojTelefona(contactPersonRequest.getBrojTelefona());
        contactPerson.setPozicija(contactPersonRequest.getPozicija());
        contactPerson.setNapomena(contactPersonRequest.getNapomena());

        return companyContactPersonRepository.save(contactPerson);
    }

    public Optional<CompanyContactPerson> getContactPersonById(Long id) {
        return companyContactPersonRepository.findById(id);
    }

    public List<CompanyContactPerson> getContactPersons(Long companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        if(company.isEmpty()) {
            throw new InvalidCompanyException(InvalidCompanyException.MESSAGE_DOES_NOT_EXIST);
        }

        return companyContactPersonRepository.findByCompany(company.get());
    }

    public void deleteContactPerson(Long id) {
        Optional<CompanyContactPerson> contactPerson = companyContactPersonRepository.findById(id);
        if(contactPerson.isEmpty()) {
            throw new InvalidCompanyException("Provided contact person does not exist");
        }
        companyContactPersonRepository.delete(contactPerson.get());
    }

}
