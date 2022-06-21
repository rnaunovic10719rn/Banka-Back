package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.exceptions.InvalidCompanyException;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.CompanyRequest;
import rs.edu.raf.banka.racun.utils.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    private void validateCompanyRequest(CompanyRequest companyRequest) {
        if(StringUtils.emptyString(companyRequest.getNaziv()) ||
                StringUtils.emptyString(companyRequest.getAdresa()) ||
                StringUtils.emptyString(companyRequest.getDrzava()) ||
                StringUtils.emptyString(companyRequest.getMaticniBroj()) ||
                StringUtils.emptyString(companyRequest.getPib()) ||
                StringUtils.emptyString(companyRequest.getSifraDelatnosti())) {
            throw new InvalidCompanyException("Required parameter is missing");
        }
    }

    public Company createCompany(CompanyRequest companyRequest) {
        if(companyRequest.getId() != null) {
            throw new InvalidCompanyException("ID is not allowed when creating a new company");
        }
        validateCompanyRequest(companyRequest);

        Company company = new Company();
        company.setNaziv(companyRequest.getNaziv());
        company.setAdresa(companyRequest.getAdresa());
        company.setDrzava(companyRequest.getDrzava());
        company.setMaticniBroj(companyRequest.getMaticniBroj());
        company.setPib(companyRequest.getPib());
        company.setSifraDelatnosti(companyRequest.getSifraDelatnosti());

        return companyRepository.save(company);
    }

    public Company editCompany(CompanyRequest companyRequest) {
        if(companyRequest.getId() == null) {
            throw new InvalidCompanyException("ID is required when editing an existing company");
        }
        validateCompanyRequest(companyRequest);

        Optional<Company> companyOptional = companyRepository.findById(companyRequest.getId());
        if(companyOptional.isEmpty()) {
            throw new InvalidCompanyException("Company with provided ID not found");
        }

        Company company = companyOptional.get();
        company.setNaziv(companyRequest.getNaziv());
        company.setAdresa(companyRequest.getAdresa());
        company.setDrzava(companyRequest.getDrzava());
        company.setMaticniBroj(companyRequest.getMaticniBroj());
        company.setPib(companyRequest.getPib());
        company.setSifraDelatnosti(companyRequest.getSifraDelatnosti());

        return companyRepository.save(company);
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Company> getCompanies() {
        return companyRepository.findAll();
    }

    public List<Company> getCompanyByNaziv(String naziv) {
        return companyRepository.findByNaziv(naziv);
    }

    public Company getCompanyByMaticniBroj(String maticniBroj) {
        return companyRepository.findByMaticniBroj(maticniBroj);
    }

    public Company getCompanyByPib(String pib) {
        return companyRepository.findByPib(pib);
    }

}
