package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransakcijaService {

    private final RacunRepository racunRepository;
    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaRepository transakcijaRepository;
    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final ValutaRepository valutaRepository;

    private final EntityManager entityManager;

    @Autowired
    public TransakcijaService(RacunRepository racunRepository,
                              SredstvaKapitalService sredstvaKapitalService,
                              TransakcijaRepository transakcijaRepository,
                              SredstvaKapitalRepository sredstvaKapitalRepository,
                              ValutaRepository valutaRepository,
                              EntityManager entityManager) {
        this.racunRepository = racunRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaRepository = transakcijaRepository;
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.valutaRepository = valutaRepository;
        this.entityManager = entityManager;
    }

    public List<Transakcija> getAll(String username){
        return transakcijaRepository.findByUsername(username);
    }

    @Transactional
    public Transakcija dodajTransakciju(String username, UUID brojRacuna, String opis, String kodValute, double uplata, double isplata, double rezervisano, double rezervisanoKoristi){
        // KORAK 1: Uzmi objekat Racuna i Valute.
        Racun racun = racunRepository.findByBrojRacuna(brojRacuna);
        if (racun == null) {
            log.error("dodajTransakciju: failed to get racun {}", brojRacuna);
            return null;
        }
        Valuta valuta = valutaRepository.findValutaByKodValute(kodValute);
        if (valuta == null) {
            log.error("dodajTransakciju: failed to get valuta {}", kodValute);
            return null;
        }
        SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacunAndValuta(racun, valuta);
        if (sredstvaKapital == null) {
            sredstvaKapitalService.pocetnoStanje(brojRacuna, kodValute, 0);
        }

        // KORAK 2: Uzmi sredstva za taj racun i ZAKLJUCAJ red.
        Query query = entityManager.createQuery("from SredstvaKapital where racun = :racun and valuta = :valuta");
        query.setParameter("racun", racun);
        query.setParameter("valuta", valuta);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<SredstvaKapital> skList = query.getResultList();
        if(skList.size() != 1) {
            log.error("dodajTransakciju: unable to find sredstvaKapital for {} and {}", racun.getBrojRacuna().toString(), kodValute);
            return null;
        }

        sredstvaKapital = skList.get(0);

        Double novoStanje = sredstvaKapital.getUkupno() + uplata - isplata;
        Double novoRezervisano = sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi;
        Double novoRaspolozivo = novoStanje - novoRezervisano;

        if(novoRaspolozivo < 0) {
            log.error("dodajTransakciju: novo raspolozivo is < 0 ({})", novoRaspolozivo);
            return null;
        }

        sredstvaKapital.setUkupno(novoStanje);
        sredstvaKapital.setRezervisano(novoRezervisano);
        sredstvaKapital.setRaspolozivo(novoRaspolozivo);

        Transakcija t = new Transakcija();
        t.setRacun(racun);
        t.setUsername(username);
        t.setValuta(valuta);
        t.setDatumVreme(new Date());
        t.setOpis(opis);
        t.setUplata(uplata);
        t.setIsplata(isplata);
        t.setRezervisano(rezervisano);
        t.setRezervisanoKoristi(rezervisanoKoristi);

        t = transakcijaRepository.save(t);
        sredstvaKapitalRepository.save(sredstvaKapital);

        return t;
    }

}
