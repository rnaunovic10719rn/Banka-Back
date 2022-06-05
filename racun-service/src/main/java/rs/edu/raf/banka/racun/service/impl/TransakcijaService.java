package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.utils.HttpUtils;

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
    private final UserService userService;

    private final EntityManager entityManager;

    @Value("${racun.user-service-url}")
    private String USER_SERVICE_URL;

    @Autowired
    public TransakcijaService(RacunRepository racunRepository,
                              SredstvaKapitalService sredstvaKapitalService,
                              TransakcijaRepository transakcijaRepository,
                              SredstvaKapitalRepository sredstvaKapitalRepository,
                              ValutaRepository valutaRepository,
                              UserService userService,
                              EntityManager entityManager) {
        this.racunRepository = racunRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaRepository = transakcijaRepository;
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.valutaRepository = valutaRepository;
        this.userService = userService;
        this.entityManager = entityManager;
    }

    public List<Transakcija> getAll(String username){
        return transakcijaRepository.findByUsername(username);
    }

    @Transactional
    public Transakcija dodajTransakciju(String token, UUID brojRacuna, String opis, String kodValute, Long orderId, double uplata, double isplata, double rezervisano, double rezervisanoKoristi, Boolean lastSegment){
        String username = userService.getUserByToken(token); //Read id from token

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

        Double rezervisanoTransakcije = transakcijaRepository.getRezervisanoForOrder(orderId);
        if (rezervisanoTransakcije == null)
            rezervisanoTransakcije = 0.0;

        Double novoStanje = sredstvaKapital.getUkupno() + uplata - isplata;
        Double novoRezervisano = 0.0;
        if (rezervisanoTransakcije + rezervisano - rezervisanoKoristi >= 0) {
            if (lastSegment && rezervisanoTransakcije + rezervisano - rezervisanoKoristi > 0) {
                rezervisano = rezervisano - (rezervisanoTransakcije + rezervisano - rezervisanoKoristi);
            }
            novoRezervisano = sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi;
        } else {
            novoRezervisano = sredstvaKapital.getRezervisano() + rezervisano - rezervisanoTransakcije;
        }
        System.err.println(novoRezervisano);
        Double novoRaspolozivo = novoStanje - novoRezervisano;
        Double limitDelta = rezervisano + (isplata-rezervisanoKoristi);

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
        t.setOrderId(orderId);
        t.setDatumVreme(new Date());
        t.setOpis(opis);
        t.setUplata(uplata);
        t.setIsplata(isplata);
        t.setRezervisano(rezervisano);
        t.setRezervisanoKoristi(rezervisanoKoristi);

        t = transakcijaRepository.save(t);
        sredstvaKapitalRepository.save(sredstvaKapital);
        HttpUtils.updateUserLimit(USER_SERVICE_URL, token, limitDelta);

        return t;
    }

}
