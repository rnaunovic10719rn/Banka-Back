package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.Ugovor;
import rs.edu.raf.banka.racun.repository.*;
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

    private final RacunRepository racunRepository;

    private final ValutaRepository valutaRepository;

    @Autowired
    public UgovorService(UgovorRepository ugovorRepository, TransakcionaStavkaRepository stavkaRepository, RacunRepository racunRepository, ValutaRepository valutaRepository)
    {
        this.ugovorRepository = ugovorRepository;
        this.stavkaRepository = stavkaRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
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

    public List<Ugovor> getAllByCompany(String company)
    {
        return ugovorRepository.findAllByCompany(company);
    }

    public List<Ugovor> getAllByCompanyDraft(String company)
    {
        return ugovorRepository.findAllByCompanyAndStatus(company, UgovorStatus.DRAFT);
    }

    public List<Ugovor> getAllByCompanyFinalized(String company)
    {
        return ugovorRepository.findAllByCompanyAndStatus(company, UgovorStatus.FINALIZED);
    }

    public Ugovor createUgovor(UgovorCreateRequest request)
    {
        var ugovor = new Ugovor();
        ugovor.setStatus(UgovorStatus.DRAFT);
        ugovor.setCompany(request.getCompany());
        ugovor.setDescription(request.getDescription());
        ugovor.setDelodavniBroj(request.getDelodavniBroj());
        ugovor.setDocumentId(-1L);

        ugovorRepository.save(ugovor);
        return ugovor;
    }

    public boolean modifyUgovor(UgovorUpdateRequest request) throws Exception {
        var ugovor = getById(request.getId());
        if(ugovor == null)
            throw new Exception("Ugovor not found");

        if(ugovor.getStatus() == UgovorStatus.FINALIZED)
            throw new Exception("Ugovor is finalized");

        var modified = false;
        if(request.getCompany() != null)
        {
            ugovor.setCompany(request.getCompany());
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

    public boolean modifyDocument(Long id, Object document) throws Exception {
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

    public void addStavka(TransakcionaStavkaCreateRequest request) throws Exception {
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

        var racun = racunRepository.findById(request.getRacunId());
        if(racun.isEmpty())
            throw new Exception("Racun not found");
        stavka.setRacun(racun.get());

        stavkaRepository.save(stavka);
        ugovor.getStavke().add(stavka);
        ugovorRepository.save(ugovor);
    }

    public boolean modifyStavka(TransakcionaStavkaUpdateRequest request) throws Exception {
        var stavka = stavkaRepository.getById(request.getStavkaId());
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

        if(request.getRacunId() != null)
        {
            var racun = racunRepository.findById(request.getRacunId());
            if(racun.isEmpty())
                throw new Exception("Racun not found");
            stavka.setRacun(racun.get());
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
