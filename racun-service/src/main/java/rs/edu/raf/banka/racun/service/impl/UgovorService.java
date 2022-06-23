package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.Ugovor;
import rs.edu.raf.banka.racun.repository.*;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaCreateRequest;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaUpdateRequest;
import rs.edu.raf.banka.racun.requests.UgovorCreateRequest;
import rs.edu.raf.banka.racun.requests.UgovorUpdateRequest;

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

    @Autowired
    public UgovorService(UgovorRepository ugovorRepository, TransakcionaStavkaRepository stavkaRepository, ValutaRepository valutaRepository, CompanyRepository companyRepository)
    {
        this.ugovorRepository = ugovorRepository;
        this.stavkaRepository = stavkaRepository;
        this.valutaRepository = valutaRepository;
        this.companyRepository = companyRepository;
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

    public List<Ugovor> getAll()
    {
        return ugovorRepository.findAll();
    }

    public List<Ugovor> getAllDraft()
    {
        return ugovorRepository.findAllByStatus(UgovorStatus.DRAFT);
    }

    public List<Ugovor> getAllFinalized()
    {
        return ugovorRepository.findAllByStatus(UgovorStatus.FINALIZED);
    }

    public List<Ugovor> getAllByCompany(Long companyId) throws Exception {
        var company = companyRepository.findById(companyId);
        if(company.isEmpty())
            throw new Exception("Company not found");
        return ugovorRepository.findAllByCompany(company.get());
    }

    public List<Ugovor> getAllByCompanyDraft(Long companyId) throws Exception {
        var company = companyRepository.findById(companyId);
        if(company.isEmpty())
            throw new Exception("Company not found");
        return ugovorRepository.findAllByCompanyAndStatus(company.get(), UgovorStatus.DRAFT);
    }

    public List<Ugovor> getAllByCompanyFinalized(Long companyPib) throws Exception {
        var company = companyRepository.findById(companyPib);
        if(company.isEmpty())
            throw new Exception("Company not found");
        return ugovorRepository.findAllByCompanyAndStatus(company.get(), UgovorStatus.FINALIZED);
    }

    public Ugovor createUgovor(UgovorCreateRequest request) throws Exception {

        if(request.getCompanyId() == null || request.getDescription() == null || request.getDelodavniBroj() == null)
            throw new Exception("bad request");

        var ugovor = new Ugovor();
        ugovor.setStatus(UgovorStatus.DRAFT);
        var company = companyRepository.findById(request.getCompanyId());
        if(company.isEmpty())
            throw new Exception("Company not found");
        ugovor.setCompany(company.get());
        ugovor.setDescription(request.getDescription());
        ugovor.setDelodavniBroj(request.getDelodavniBroj());
        ugovor.setDocumentId(-1L);

        ugovorRepository.save(ugovor);
        return ugovor;
    }

    public boolean modifyUgovor(UgovorUpdateRequest request) throws Exception {

        if(request.getCompanyId() == null && request.getDescription() == null && request.getDelodavniBroj() == null)
            throw new Exception("bad request");

        var ugovor = getById(request.getId());
        if(ugovor == null)
            throw new Exception("Ugovor not found");

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
        if(request.getDelodavniBroj() != null)
        {
            ugovor.setDelodavniBroj(request.getDelodavniBroj());
            modified = true;
        }

        if(modified)
            ugovorRepository.save(ugovor);
        return modified;
    }

    public boolean modifyDocument(Long id, MultipartFile file) throws Exception {
        if (file == null)
            throw new Exception("bad request");
        var ugovor = getById(id);
        if(ugovor == null)
            throw new Exception("Ugovor not found");
        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        //TODO: document save logic
        Long documentID = -1L;
        ugovor.setDocumentId(documentID);
        ugovorRepository.save(ugovor);
        return true;
    }

    public TransakcionaStavka addStavka(TransakcionaStavkaCreateRequest request) throws Exception {

        if(request.getUgovorId() == null || request.getCenaHartije() == null || request.getHartijaId() == null
                || request.getKolicina() == null || request.getHartijaType() == null || request.getType() == null
                || request.getRacunType() == null || request.getValuta() == null)
            throw new Exception("bad request");


        var ugovor = getById(request.getUgovorId());
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        var stavka = new TransakcionaStavka();
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

    public boolean modifyStavka(TransakcionaStavkaUpdateRequest request) throws Exception {

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

    public boolean removeStavka(Long id) throws Exception {
        var stavka = getTransakcionaStavkaById(id);
        if(stavka == null)
            throw new Exception("Transakciona stavka not found");

        //TODO: Test if stavka.ugovor is properly connected
        var ugovor = stavka.getUgovor();
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        //TODO: Check if removing stavka updates ugovor stavke
        ugovor.getStavke().remove(stavka);
        ugovorRepository.save(ugovor);
        stavkaRepository.delete(stavka);
        return true;
    }

    public void finalizeUgovor(Ugovor ugovor) throws Exception {
        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        ugovor.setStatus(UgovorStatus.FINALIZED);
        ugovorRepository.save(ugovor);
    }

}
