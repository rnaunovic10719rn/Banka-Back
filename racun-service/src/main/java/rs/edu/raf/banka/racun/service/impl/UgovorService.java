package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;
import rs.edu.raf.banka.racun.model.contract.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.*;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.repository.contract.TransakcionaStavkaRepository;
import rs.edu.raf.banka.racun.repository.contract.UgovorRepository;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaCreateRequest;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaUpdateRequest;
import rs.edu.raf.banka.racun.requests.UgovorCreateRequest;
import rs.edu.raf.banka.racun.requests.UgovorUpdateRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UgovorService
{
    private final UgovorRepository ugovorRepository;

    private final TransakcionaStavkaRepository stavkaRepository;

    private final ValutaRepository valutaRepository;

    private final CompanyRepository companyRepository;

    private final UserService userService;

    private final ContractDocumentService contractDocumentService;

    @Autowired
    public UgovorService(UgovorRepository ugovorRepository, TransakcionaStavkaRepository stavkaRepository, ValutaRepository valutaRepository, CompanyRepository companyRepository, UserService userService, ContractDocumentService contractDocumentService)
    {
        this.ugovorRepository = ugovorRepository;
        this.stavkaRepository = stavkaRepository;
        this.valutaRepository = valutaRepository;
        this.companyRepository = companyRepository;
        this.userService = userService;
        this.contractDocumentService = contractDocumentService;
    }


    public Ugovor getById(Long id)
    {
        var ugovor = ugovorRepository.findById(id);
        if(!ugovor.isPresent())
            return null;
        return ugovor.get();
    }

    public TransakcionaStavka getTransakcionaStavkaById(Long id)
    {
        var ugovor = stavkaRepository.findById(id);
        if(!ugovor.isPresent())
            return null;
        return ugovor.get();
    }

    private UserDto getUserByToken(String token) throws Exception {
        var user = userService.getUserByToken(token);
        if(user == null)
            throw new Exception("Invalid token");
        return user;
    }

    private boolean isUserSupervisor(UserDto user)
    {
        return user.getRoleName().equals("ROLE_SUPERVISOR")
                || user.getRoleName().equals("ROLE_ADMIN");
    }

    private boolean isUserAgent(UserDto user)
    {
        return user.getRoleName().equals("ROLE_AGENT");
    }

    private UserDto checkPermissions(Ugovor ugovor, String token) throws Exception {

        var user = getUserByToken(token);

        if(isUserSupervisor(user) || (isUserAgent(user) && ugovor.getUserId() == user.getId()))
            return user;

        throw new Exception("No permissions");
    }

    private void checkPermissions(String token) throws Exception {

        var user = getUserByToken(token);
        if(isUserSupervisor(user) || isUserAgent(user))
            return;

        throw new Exception("No permissions");
    }

    public Ugovor getUgovorById(Long id, String token) throws Exception {
        var ugovor = getById(id);
        if(ugovor == null)
            throw new Exception("Ugovor not found");
        checkPermissions(ugovor, token);
        return ugovor;
    }

    public List<Ugovor> getAll(String token) throws Exception {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAll();
        if(isUserAgent(user))
            return ugovorRepository.findAllByUserId(user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllDraft(String token) throws Exception
    {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByStatus(UgovorStatus.DRAFT);
        if(isUserAgent(user))
            return ugovorRepository.findAllByStatusAndUserId(UgovorStatus.DRAFT, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllFinalized(String token) throws Exception
    {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByStatus(UgovorStatus.FINALIZED);
        if(isUserAgent(user))
            return ugovorRepository.findAllByStatusAndUserId(UgovorStatus.FINALIZED, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByCompany(Long companyId, String token) throws Exception {
        var company = companyRepository.findById(companyId);
        if(company.isEmpty())
            throw new Exception("Company not found");

        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByCompany(company.get());
        if(isUserAgent(user))
            return ugovorRepository.findAllByCompanyAndUserId(company.get(), user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByCompanyDraft(Long companyId, String token) throws Exception {
        var company = companyRepository.findById(companyId);
        if(company.isEmpty())
            throw new Exception("Company not found");
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByCompanyAndStatus(company.get(), UgovorStatus.DRAFT);
        if(isUserAgent(user))
            return ugovorRepository.findAllByCompanyAndStatusAndUserId(company.get(), UgovorStatus.DRAFT, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByCompanyFinalized(Long companyPib, String token) throws Exception {
        var company = companyRepository.findById(companyPib);
        if(company.isEmpty())
            throw new Exception("Company not found");
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByCompanyAndStatus(company.get(), UgovorStatus.FINALIZED);
        if(isUserAgent(user))
            return ugovorRepository.findAllByCompanyAndStatusAndUserId(company.get(), UgovorStatus.FINALIZED, user.getId());
        return new ArrayList<>();
    }

    public Ugovor createUgovor(UgovorCreateRequest request, String token) throws Exception {

        checkPermissions(token);
        if(request.getCompanyId() == null || request.getDescription() == null || request.getDelovodniBroj() == null)
            throw new Exception("bad request");

        var ugovor = new Ugovor();
        ugovor.setStatus(UgovorStatus.DRAFT);
        var company = companyRepository.findById(request.getCompanyId());
        if(company.isEmpty())
            throw new Exception("Company not found");
        ugovor.setCompany(company.get());
        ugovor.setDescription(request.getDescription());
        ugovor.setDelovodniBroj(request.getDelovodniBroj());
        ugovor.setDocumentId("");

        ugovorRepository.save(ugovor);
        return ugovor;
    }

    public Ugovor modifyUgovor(UgovorUpdateRequest request, String token) throws Exception {

        if(request.getCompanyId() == null && request.getDescription() == null && request.getDelovodniBro() == null)
            throw new Exception("bad request");

        var ugovor = getById(request.getId());
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        checkPermissions(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        var modified = false;
        if(request.getCompanyId() != null)
        {
            var company = companyRepository.findById(request.getCompanyId());
            if(company.isEmpty())
                throw new Exception("Company not found");
            ugovor.setCompany(company.get());
            modified = true;
        }
        if(request.getDescription() != null)
        {
            ugovor.setDescription(request.getDescription());
            modified = true;
        }
        if(request.getDelovodniBro() != null)
        {
            ugovor.setDelovodniBroj(request.getDelovodniBro());
            modified = true;
        }

        if(modified)
            ugovor = ugovorRepository.save(ugovor);

        return ugovor;
    }

    public Ugovor finalizeUgovor(Long id, MultipartFile document, String token) throws Exception {
        if (document == null)
            throw new Exception("bad request");
        var ugovor = getById(id);
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        checkPermissions(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        String documentId = contractDocumentService.saveDocument(ugovor, document);
        ugovor.setDocumentId(documentId);
        ugovor.setStatus(UgovorStatus.FINALIZED);
        ugovor = ugovorRepository.save(ugovor);

        return ugovor;
    }

    public Binary getContractDocument(Long id) throws Exception {
        if(id == null)
            throw new Exception("Invalid contract ID");

        var ugovor = getById(id);
        if(ugovor == null)
            throw new Exception("Ugovor not found");
        if(ugovor.getStatus() != UgovorStatus.FINALIZED || ugovor.getDocumentId() == null || ugovor.getDocumentId().isBlank())
            throw new Exception("Contract not found");

        ContractDocument contractDocument = contractDocumentService.getDocument(ugovor.getDocumentId());

        return contractDocument.getDocument();
    }

    public TransakcionaStavka addStavka(TransakcionaStavkaCreateRequest request, String token) throws Exception {

        if(request.getUgovorId() == null || request.getCenaHartije() == null || request.getHartijaId() == null
                || request.getKolicina() == null || request.getHartijaType() == null || request.getType() == null
                || request.getRacunType() == null || request.getValuta() == null)
            throw new Exception("bad request");


        var ugovor = getById(request.getUgovorId());
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        var user = checkPermissions(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        var stavka = new TransakcionaStavka();
        stavka.setUserId(user.getId());
        stavka.setCenaHartije(request.getCenaHartije());
        stavka.setHartijaId(request.getHartijaId());
        stavka.setHartijaType(request.getHartijaType());
        stavka.setType(request.getType());
        stavka.setKolicina(request.getKolicina());
        stavka.setCenaHartije(request.getCenaHartije());

        var valuta = valutaRepository.findValutaByKodValute(request.getValuta());
        if(valuta == null)
            throw new Exception("Valuta not found");
        stavka.setValuta(valuta);

        stavka.setRacunType(request.getRacunType());

        stavkaRepository.save(stavka);
        ugovor.getStavke().add(stavka);
        ugovorRepository.save(ugovor);
        return stavka;
    }

    public boolean modifyStavka(TransakcionaStavkaUpdateRequest request, String token) throws Exception {

        if(request.getStavkaId() == null && request.getCenaHartije() == null && request.getHartijaId() == null
                && request.getKolicina() == null && request.getHartijaType() == null && request.getType() == null
                && request.getRacunType() == null && request.getValuta() == null)
            throw new Exception("bad request");

        var stavka = getTransakcionaStavkaById(request.getStavkaId());
        if(stavka == null)
            throw new Exception("Stavka not found");

        var ugovor = stavka.getUgovor();
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        checkPermissions(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        var modified = false;

        if(request.getCenaHartije() != null)
        {
            stavka.setCenaHartije(request.getCenaHartije());
            modified = true;
        }
        if(request.getHartijaId() != null)
        {
            stavka.setHartijaId(request.getHartijaId());
            modified = true;
        }
        if(request.getHartijaType() != null)
        {
            stavka.setHartijaType(request.getHartijaType());
            modified = true;
        }
        if(request.getType() != null)
        {
            stavka.setType(request.getType());
            modified = true;
        }
        if(request.getKolicina() != null)
        {
            stavka.setKolicina(request.getKolicina());
            modified = true;
        }
        if(request.getCenaHartije() != null)
        {
            stavka.setCenaHartije(request.getCenaHartije());
            modified = true;
        }

        if(request.getValuta() != null)
        {
            var valuta = valutaRepository.findValutaByKodValute(request.getValuta());
            if(valuta == null)
                throw new Exception("Valuta not found");
            stavka.setValuta(valuta);
            modified = true;
        }

        if(request.getRacunType() != null)
        {
            stavka.setRacunType(request.getRacunType());
            modified = true;
        }

        if(modified)
        {
            //TODO: Check if stavka modification triggers ugovor modification
            ugovor.setLastChanged(new Date());
            stavkaRepository.save(stavka);
            ugovorRepository.save(ugovor);
        }
        return modified;
    }

    public boolean removeStavka(Long id, String token) throws Exception {
        var stavka = getTransakcionaStavkaById(id);
        if(stavka == null)
            throw new Exception("Transakciona stavka not found");

        //TODO: Test if stavka.ugovor is properly connected
        var ugovor = stavka.getUgovor();
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        checkPermissions(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        //TODO: Check if removing stavka updates ugovor stavke
        ugovor.getStavke().remove(stavka);
        ugovorRepository.save(ugovor);
        stavkaRepository.delete(stavka);
        return true;
    }

}
