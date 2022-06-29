package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.exceptions.ContractExpcetion;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;
import rs.edu.raf.banka.racun.model.contract.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.*;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.repository.contract.TransakcionaStavkaRepository;
import rs.edu.raf.banka.racun.repository.contract.UgovorRepository;
import rs.edu.raf.banka.racun.requests.*;
import rs.edu.raf.banka.racun.response.AskBidPriceResponse;
import rs.edu.raf.banka.racun.utils.HttpUtils;
import rs.edu.raf.banka.racun.utils.StringUtils;

import java.io.IOException;
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

    private final TransakcijaService transakcijaService;

    private final RacunRepository racunRepository;

    @Value("${racun.berza-service-baseurl}")
    private String BERZA_SERVICE_BASE_URL;

    @Autowired
    public UgovorService(UgovorRepository ugovorRepository, TransakcionaStavkaRepository stavkaRepository, ValutaRepository valutaRepository, CompanyRepository companyRepository, UserService userService, ContractDocumentService contractDocumentService, TransakcijaService transakcijaService, RacunRepository racunRepository)
    {
        this.ugovorRepository = ugovorRepository;
        this.stavkaRepository = stavkaRepository;
        this.valutaRepository = valutaRepository;
        this.companyRepository = companyRepository;
        this.userService = userService;
        this.contractDocumentService = contractDocumentService;
        this.transakcijaService = transakcijaService;
        this.racunRepository = racunRepository;
    }


    public Ugovor getById(Long id) {
        var ugovor = ugovorRepository.findById(id);
        if(!ugovor.isPresent())
            return null;
        return ugovor.get();
    }

    private TransakcionaStavka getTransakcionaStavkaById(Long id) throws ContractExpcetion {
        var stavka = stavkaRepository.findById(id);
        if(stavka.isEmpty())
            throw new ContractExpcetion("Transakciona stavka not found");
        return stavka.get();
    }

    public TransakcionaStavka getTransakcionaStavkaById(Long id, String token) throws ContractExpcetion {
        var stavka =getTransakcionaStavkaById(id);
        var ugovor = stavka.getUgovor();
        if(ugovor == null)
            throw new ContractExpcetion("Ugovor not found");
        checkUserCanAccessUgovor(ugovor, token);
        return stavka;
    }


    private UserDto getUserByToken(String token) throws ContractExpcetion {
        var user = userService.getUserByToken(token);
        if(user == null)
            throw new ContractExpcetion("Invalid token");
        return user;
    }

    private boolean isUserSupervisor(UserDto user)
    {
        return user.getRoleName().equals("ROLE_SUPERVISOR")
                || user.getRoleName().equals("ROLE_ADMIN")
                || user.getRoleName().equals("ROLE_GL_ADMIN");
    }

    private boolean isUserAgent(UserDto user)
    {
        return user.getRoleName().equals("ROLE_AGENT");
    }

    private UserDto checkUserCanAccessUgovor(Ugovor ugovor, String token) throws ContractExpcetion {

        var user = getUserByToken(token);

        if(isUserSupervisor(user) || (isUserAgent(user) && ugovor.getUserId() == user.getId()))
            return user;

        throw new ContractExpcetion("No permissions");
    }

    private UserDto checkUserCanFinalizeUgovor(Ugovor ugovor, String token) throws ContractExpcetion {

        var user = getUserByToken(token);

        if(isUserSupervisor(user))
            return user;

        throw new ContractExpcetion("No permissions");
    }

    public Ugovor getUgovorById(Long id, String token) throws ContractExpcetion {
        var ugovor = getById(id);
        if(ugovor == null)
            throw new ContractExpcetion("Ugovor not found");
        checkUserCanAccessUgovor(ugovor, token);
        return ugovor;
    }

    public List<Ugovor> getAll(String token) throws ContractExpcetion {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAll();
        if(isUserAgent(user))
            return ugovorRepository.findAllByUserId(user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllDraft(String token) throws ContractExpcetion
    {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByStatus(UgovorStatus.DRAFT);
        if(isUserAgent(user))
            return ugovorRepository.findAllByStatusAndUserId(UgovorStatus.DRAFT, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllFinalized(String token) throws ContractExpcetion
    {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByStatus(UgovorStatus.FINALIZED);
        if(isUserAgent(user))
            return ugovorRepository.findAllByStatusAndUserId(UgovorStatus.FINALIZED, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByCompany(Long companyId, String token) throws ContractExpcetion {
        var company = companyRepository.findById(companyId);
        if(company.isEmpty())
            throw new ContractExpcetion("Company not found");

        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByCompany(company.get());
        if(isUserAgent(user))
            return ugovorRepository.findAllByCompanyAndUserId(company.get(), user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByCompanyAndUgovorStatus(Long companyId, String token, UgovorStatus status) throws ContractExpcetion {
        var company = companyRepository.findById(companyId);
        if(company.isEmpty())
            throw new ContractExpcetion("Company not found");
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByCompanyAndStatus(company.get(), status);
        if(isUserAgent(user))
            return ugovorRepository.findAllByCompanyAndStatusAndUserId(company.get(), status, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByDelovodniBroj(String delovodniBroj, String token) throws ContractExpcetion {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByDelovodniBroj(delovodniBroj);
        if(isUserAgent(user))
            return ugovorRepository.findAllByDelovodniBrojAndUserId(delovodniBroj, user.getId());
        return new ArrayList<>();
    }

    public List<Ugovor> getAllByDelovodniBrojAndUgovorStatus(String delovodniBroj, String token, UgovorStatus status) throws ContractExpcetion {
        var user = getUserByToken(token);
        if(isUserSupervisor(user))
            return ugovorRepository.findAllByDelovodniBrojAndStatus(delovodniBroj, status);
        if(isUserAgent(user))
            return ugovorRepository.findAllByDelovodniBrojAndStatusAndUserId(delovodniBroj, status, user.getId());
        return new ArrayList<>();
    }

    public Ugovor createUgovor(UgovorCreateRequest request, String token) throws ContractExpcetion {

        var user = getUserByToken(token);
        if(request.getCompanyId() == null || request.getDescription() == null || request.getDelovodniBroj() == null)
            throw new ContractExpcetion("bad request");

        var ugovor = new Ugovor();
        ugovor.setUserId(user.getId());
        ugovor.setStatus(UgovorStatus.DRAFT);
        var company = companyRepository.findById(request.getCompanyId());
        if(company.isEmpty())
            throw new ContractExpcetion("Company not found");
        ugovor.setCompany(company.get());
        ugovor.setDescription(request.getDescription());
        ugovor.setDelovodniBroj(request.getDelovodniBroj());
        ugovor.setDocumentId("");

        ugovorRepository.save(ugovor);
        return ugovor;
    }

    public Ugovor modifyUgovor(UgovorUpdateRequest request, String token) throws ContractExpcetion {

        if(request.getCompanyId() == null && request.getDescription() == null && request.getDelovodniBroj() == null)
            throw new ContractExpcetion("bad request");

        var ugovor = getById(request.getId());
        if(ugovor == null)
            throw new ContractExpcetion("Ugovor not found");

        checkUserCanAccessUgovor(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new ContractExpcetion("Ugovor is finalized");

        var modified = false;
        if(request.getCompanyId() != null)
        {
            var company = companyRepository.findById(request.getCompanyId());
            if(company.isEmpty())
                throw new ContractExpcetion("Company not found");
            ugovor.setCompany(company.get());
            modified = true;
        }
        if(request.getDescription() != null)
        {
            ugovor.setDescription(request.getDescription());
            modified = true;
        }
        if(request.getDelovodniBroj() != null)
        {
            ugovor.setDelovodniBroj(request.getDelovodniBroj());
            modified = true;
        }

        if(modified)
            ugovor = ugovorRepository.save(ugovor);

        return ugovor;
    }

    public Ugovor finalizeUgovor(Long id, MultipartFile document, String token) throws ContractExpcetion, IOException {
        if (document == null)
            throw new ContractExpcetion("bad request");
        var ugovor = getById(id);
        if(ugovor == null)
            throw new ContractExpcetion("Ugovor not found");

        checkUserCanFinalizeUgovor(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new ContractExpcetion("Ugovor is finalized");

        String documentId = contractDocumentService.saveDocument(ugovor, document);

        finalizeTranactions(token, ugovor.getStavke());

        ugovor.setDocumentId(documentId);
        ugovor.setStatus(UgovorStatus.FINALIZED);
        ugovor = ugovorRepository.save(ugovor);

        return ugovor;
    }

    private void finalizeTranactions(String token, List<TransakcionaStavka> stavke) {
        for(var stavka: stavke) {
            TransakcijaRequest finalizeRequestPotrazna = finalizeStavkaTransaction(stavka, token, true);
            submitTransaction(finalizeRequestPotrazna, token);

            TransakcijaRequest finalizeRequestDugovna = finalizeStavkaTransaction(stavka, token, false);
            submitTransaction(finalizeRequestDugovna, token);
        }
    }

    public Ugovor rejectUgovor(Long id, String token) throws ContractExpcetion {
        Ugovor ugovor = getById(id);
        if(ugovor == null)
            throw new ContractExpcetion("Ugovor not found");

        checkUserCanAccessUgovor(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new ContractExpcetion("Ugovor is finalized");

        rejectTransactions(token, ugovor.getStavke());

        ugovor.setStatus(UgovorStatus.REJECTED);
        ugovor = ugovorRepository.save(ugovor);

        return ugovor;
    }

    private void rejectTransactions(String token, List<TransakcionaStavka> stavke) {
        for(var stavka: stavke) {
            TransakcijaRequest deleteTransakcija = deleteStavkaTransaction(stavka, token);
            submitTransaction(deleteTransakcija, token);
        }
    }

    public Binary getContractDocument(Long id, String token) throws ContractExpcetion {
        if(id == null)
            throw new ContractExpcetion("Invalid contract ID");

        var ugovor = getById(id);
        if(ugovor == null)
            throw new ContractExpcetion("Ugovor not found");

        checkUserCanAccessUgovor(ugovor, token);

        if(ugovor.getStatus() != UgovorStatus.FINALIZED || ugovor.getDocumentId() == null || ugovor.getDocumentId().isBlank())
            throw new ContractExpcetion("Contract not found");

        ContractDocument contractDocument = contractDocumentService.getDocument(ugovor.getDocumentId());

        return contractDocument.getDocument();
    }

    public TransakcionaStavka addStavka(TransakcionaStavkaRequest request, String token) throws ContractExpcetion {
        if(request.getUgovorId() == null ||
                request.getStavkaId() != null ||
                request.getKapitalTypePotrazni() == null ||
                request.getKapitalTypeDugovni() == null ||
                StringUtils.emptyString(request.getKapitalPotrazniOznaka()) ||
                StringUtils.emptyString(request.getKapitalDugovniOznaka()) ||
                request.getKolicinaPotrazna() == null ||
                request.getKolicinaDugovna() == null) {
            throw new ContractExpcetion("bad request");
        }

        Ugovor ugovor = getById(request.getUgovorId());
        if(ugovor == null) {
            throw new ContractExpcetion("Ugovor not found");
        }

        UserDto user = checkUserCanAccessUgovor(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED) {
            throw new ContractExpcetion("Ugovor is finalized");
        }

        validateAndCompleteRequest(request);

        TransakcionaStavka stavka = new TransakcionaStavka();
        stavka.setUgovor(ugovor);
        stavka.setUserId(user.getId());

        stavka.setKapitalTypePotrazni(request.getKapitalTypePotrazni());
        stavka.setKapitalPotrazniId(request.getKapitalPotrazniId());
        stavka.setKapitalPotrazniOznaka(request.getKapitalPotrazniOznaka());
        stavka.setKolicinaPotrazna(request.getKolicinaPotrazna());

        stavka.setKapitalTypeDugovni(request.getKapitalTypeDugovni());
        stavka.setKapitalDugovniId(request.getKapitalDugovniId());
        stavka.setKapitalDugovniOznaka(request.getKapitalDugovniOznaka());
        stavka.setKolicinaDugovna(request.getKolicinaDugovna());

        TransakcijaRequest createRequest = createStavkaTransaction(stavka, token);
        if(!submitTransaction(createRequest, token)) {
            throw new ContractExpcetion("Transaction error");
        }

        return stavkaRepository.save(stavka);
    }

    public TransakcionaStavka modifyStavka(TransakcionaStavkaRequest request, String token) throws ContractExpcetion {
        if(request.getStavkaId() == null ||
                request.getUgovorId() != null ||
                request.getKapitalTypePotrazni() == null ||
                request.getKapitalTypeDugovni() == null ||
                StringUtils.emptyString(request.getKapitalPotrazniOznaka()) ||
                StringUtils.emptyString(request.getKapitalDugovniOznaka()) ||
                request.getKolicinaPotrazna() == null ||
                request.getKolicinaDugovna() == null) {
            throw new ContractExpcetion("bad request");
        }

        TransakcionaStavka stavka = getTransakcionaStavkaById(request.getStavkaId());
        Ugovor ugovor = stavka.getUgovor();
        if(ugovor == null) {
            throw new ContractExpcetion("Ugovor not found");
        }

        checkUserCanAccessUgovor(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED) {
            throw new ContractExpcetion("Ugovor is finalized");
        }

        validateAndCompleteRequest(request);

        var originalStavka = new TransakcionaStavka(stavka);

        boolean modified = false;

        if(request.getKapitalTypePotrazni() != null && !originalStavka.getKapitalTypePotrazni().equals(request.getKapitalTypePotrazni())) {
            stavka.setKapitalTypePotrazni(request.getKapitalTypePotrazni());
            modified = true;
        }
        if(request.getKapitalPotrazniOznaka() != null && !originalStavka.getKapitalPotrazniId().equals(request.getKapitalPotrazniId())) {
            stavka.setKapitalPotrazniId(request.getKapitalPotrazniId());
            stavka.setKapitalPotrazniOznaka(request.getKapitalPotrazniOznaka());
            modified = true;
        }
        if(request.getKolicinaPotrazna() != null && !originalStavka.getKolicinaPotrazna().equals(request.getKolicinaPotrazna())) {
            stavka.setKolicinaPotrazna(request.getKolicinaPotrazna());
            modified = true;
        }
        if(request.getKapitalTypeDugovni() != null && !originalStavka.getKapitalTypeDugovni().equals(request.getKapitalTypeDugovni())) {
            stavka.setKapitalTypeDugovni(request.getKapitalTypeDugovni());
            modified = true;
        }
        if(request.getKapitalDugovniOznaka() != null && !originalStavka.getKapitalDugovniId().equals(request.getKapitalDugovniId())) {
            stavka.setKapitalDugovniId(request.getKapitalDugovniId());
            stavka.setKapitalDugovniOznaka(request.getKapitalDugovniOznaka());
            modified = true;
        }
        if(request.getKolicinaDugovna() != null && !originalStavka.getKolicinaDugovna().equals(request.getKolicinaDugovna())) {
            stavka.setKolicinaDugovna(request.getKolicinaDugovna());
            modified = true;
        }

        if(modified) {
            TransakcijaRequest deleteRequest = deleteStavkaTransaction(originalStavka, token);
            if(!submitTransaction(deleteRequest, token))
                throw new ContractExpcetion("Transaction error");
            var createRequest = createStavkaTransaction(stavka, token);
            if(!submitTransaction(createRequest, token)) {
                var restoreRequest = createStavkaTransaction(originalStavka, token);
                submitTransaction(restoreRequest, token);
                throw new ContractExpcetion("Transaction error");
            }

            ugovor.setLastChanged(new Date());
            stavka = stavkaRepository.save(stavka);
            ugovorRepository.save(ugovor);
        }

        return stavka;
    }

    public TransakcionaStavka removeStavka(Long id, String token) throws ContractExpcetion {
        TransakcionaStavka stavka = getTransakcionaStavkaById(id);
        Ugovor ugovor = stavka.getUgovor();
        if(ugovor == null) {
            throw new ContractExpcetion("Ugovor not found");
        }

        checkUserCanAccessUgovor(ugovor, token);

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new ContractExpcetion("Ugovor is finalized");

        var deleteRequest = deleteStavkaTransaction(stavka, token);
        if(!submitTransaction(deleteRequest, token)) {
            throw new ContractExpcetion("Transaction error");
        }

        ugovor.setLastChanged(new Date());
        ugovorRepository.save(ugovor);
        stavkaRepository.delete(stavka);

        return stavka;
    }

    private void validateAndCompleteRequest(TransakcionaStavkaRequest createRequest) throws ContractExpcetion {
        // Potrazna
        if(createRequest.getKapitalTypePotrazni().equals(KapitalType.NOVAC)) {
            // Provera valute
            Valuta valuta = valutaRepository.findValutaByKodValute(createRequest.getKapitalPotrazniOznaka());
            if(valuta == null) {
                throw new ContractExpcetion("Currency not found");
            }
            createRequest.setKapitalPotrazniId(valuta.getId());
        } else {
            AskBidPriceResponse askBidPriceResponse = getAskBidPrice(createRequest.getKapitalTypePotrazni(), createRequest.getKapitalPotrazniOznaka());
            if(askBidPriceResponse == null) {
                throw new ContractExpcetion("Security not found");
            }
            createRequest.setKapitalPotrazniId(askBidPriceResponse.getHartijaId());
        }

        // Dugovna
        if(createRequest.getKapitalTypeDugovni().equals(KapitalType.NOVAC)) {
            // Provera valute
            Valuta valuta = valutaRepository.findValutaByKodValute(createRequest.getKapitalDugovniOznaka());
            if(valuta == null) {
                throw new ContractExpcetion("Currency not found");
            }
            createRequest.setKapitalDugovniId(valuta.getId());
        } else {
            AskBidPriceResponse askBidPriceResponse = getAskBidPrice(createRequest.getKapitalTypeDugovni(), createRequest.getKapitalDugovniOznaka());
            if(askBidPriceResponse == null) {
                throw new ContractExpcetion("Security not found");
            }
            createRequest.setKapitalDugovniId(askBidPriceResponse.getHartijaId());
        }
    }

    private AskBidPriceResponse getAskBidPrice(KapitalType kapitalType, String symbol) {
        String type = "";
        switch (kapitalType) {
            case AKCIJA -> type = "AKCIJA";
            case FOREX -> type = "FOREX";
            case FUTURE_UGOVOR -> type = "FUTURES_UGOVOR";
        }
        ResponseEntity<AskBidPriceResponse> resp = HttpUtils.getAskBidPrice(BERZA_SERVICE_BASE_URL, type, symbol);
        if(!resp.getStatusCode().equals(HttpStatus.OK)) {
            return null;
        }
        return resp.getBody();
    }

    private boolean submitTransaction(TransakcijaRequest transakcijaRequest, String token) {
        return transakcijaService.dodajTransakciju(token, transakcijaRequest) != null;
    }

    private TransakcijaRequest baseRequest(TransakcionaStavka stavka, String token, boolean isPotrazna) {
        var request = new TransakcijaRequest();
        if(isPotrazna) {
            request.setType(stavka.getKapitalTypePotrazni());
            if(stavka.getKapitalTypePotrazni() != KapitalType.NOVAC) {
                request.setHartijaId(stavka.getKapitalPotrazniId());
                request.setUnitPrice(0.0);
            } else {
                Valuta valuta = valutaRepository.getById(stavka.getKapitalPotrazniId());
                request.setValutaOznaka(valuta.getKodValute());
            }
        } else { // Ako nije potrazna, onda je dugovna strana
            request.setType(stavka.getKapitalTypeDugovni());
            if(stavka.getKapitalTypeDugovni() != KapitalType.NOVAC) {
                request.setHartijaId(stavka.getKapitalDugovniId());
                request.setUnitPrice(0.0);
            } else {
                Valuta valuta = valutaRepository.getById(stavka.getKapitalDugovniId());
                request.setValutaOznaka(valuta.getKodValute());
            }
        }

        // OTC (uglavnom) nikada ne koristi margine
        request.setMargins(false);
        request.setUsername(userService.getUsernameByToken(token));
        request.setUplata(0.0);
        request.setIsplata(0.0);
        request.setRezervisano(0.0);
        request.setOrderId(-1L);

        return request;
    }

    private TransakcijaRequest createStavkaTransaction(TransakcionaStavka stavka, String token) {
        TransakcijaRequest transakcijaRequest = baseRequest(stavka, token, true);
        transakcijaRequest.setRezervisano(stavka.getKolicinaPotrazna());
        transakcijaRequest.setOpis("Rezervacija za ugovor " + stavka.getUgovor().getDelovodniBroj());
        return transakcijaRequest;
    }

    private TransakcijaRequest deleteStavkaTransaction(TransakcionaStavka stavka, String token) {
        TransakcijaRequest transakcijaRequest = baseRequest(stavka, token, true);
        transakcijaRequest.setRezervisano(-stavka.getKolicinaPotrazna());
        transakcijaRequest.setOpis("Izmena ugovora " + stavka.getUgovor().getDelovodniBroj());
        return transakcijaRequest;
    }

    private TransakcijaRequest finalizeStavkaTransaction(TransakcionaStavka stavka, String token, boolean isPotrazna) {
        TransakcijaRequest transakcijaRequest = baseRequest(stavka, token, isPotrazna);
        transakcijaRequest.setOpis("Realizacija ugovora " + stavka.getUgovor().getDelovodniBroj());
        if(isPotrazna) {
            transakcijaRequest.setIsplata(stavka.getKolicinaPotrazna());
        } else {
            transakcijaRequest.setUplata(stavka.getKolicinaDugovna());
            if(stavka.getKapitalTypePotrazni().equals(KapitalType.NOVAC) && !stavka.getKapitalTypeDugovni().equals(KapitalType.NOVAC)) {
                transakcijaRequest.setUnitPrice(stavka.getKolicinaPotrazna() / stavka.getKolicinaDugovna());
            }
        }
        return transakcijaRequest;
    }

}
