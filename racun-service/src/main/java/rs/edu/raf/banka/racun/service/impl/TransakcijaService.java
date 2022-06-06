package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.ForexPodaciDto;
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

    @Value("${racun.forex-quote-url}")
    private String FOREX_EXCHANGE_RATE_URL;

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

    public List<Transakcija> getAll(String token) {
        String role = userService.getRoleByToken(token);
        String username = userService.getUserByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username);
        else
            return transakcijaRepository.getAll();
    }

    public List<Transakcija> getAll(String token, Date odFilter, Date doFilter){
        String role = userService.getRoleByToken(token);
        String username = userService.getUserByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username, odFilter, doFilter);
        else
            return transakcijaRepository.getAll(odFilter, doFilter);
    }

    public List<Transakcija> getAll(String token, String valuta){
        String role = userService.getRoleByToken(token);
        String username = userService.getUserByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username, valuta);
        else
            return transakcijaRepository.getAll(valuta);
    }

    public List<Transakcija> getAll(String token, String valuta, Date odFilter, Date doFilter){
        String role = userService.getRoleByToken(token);
        String username = userService.getUserByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username, valuta, odFilter, doFilter);
        else
            return transakcijaRepository.getAll(valuta, odFilter, doFilter);
    }

    @Transactional
    public Transakcija dodajTransakciju(String token, UUID brojRacuna, String opis, String kodValute, Long orderId, double uplata, double isplata, double rezervisano, Boolean lastSegment){
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

        // Izracunaj novo ukupno stanje na racunu
        Double novoStanje = sredstvaKapital.getUkupno() + uplata - isplata;

        // Izracunaj koliko se rezervisanih sredstava koristi u transakciji
        Double rezervisanoKoristi = 0.0;
        // Pokupi koliko je preostalo rezervisanih sredstava za taj order
        Double rezervisanoForOrder = transakcijaRepository.getRezervisanoForOrder(orderId);
        if (rezervisanoForOrder != null) {
            if(rezervisanoForOrder > isplata) {
                rezervisanoKoristi = isplata;
            } else {
                rezervisanoKoristi = rezervisanoForOrder;
            }
        }

        // Ukoliko je poslednji segmet, proveri da li treba izvrsiti povracaj rezervisanih sredstava, u slucaju da je ostao visak
        if (lastSegment && rezervisanoForOrder != null && rezervisanoForOrder + rezervisano - rezervisanoKoristi > 0) {
            rezervisano += -1 * (rezervisanoForOrder - rezervisanoKoristi);
        }

        // Izracunaj novo rezervisano i raspolozivo
        Double novoRezervisano = sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi;
        Double novoRaspolozivo = novoStanje - novoRezervisano;

        // Izracunaj promenu limita korisnika
        Double limitDelta = rezervisano + (isplata-rezervisanoKoristi);

        // Provera da li je novo raspolozivo u plusu (ako nije, nemamo sredstva na racunu, pa treba odbiti transakciju)
        if(novoRaspolozivo < 0) {
            log.error("dodajTransakciju: novo raspolozivo is < 0 ({})", novoRaspolozivo);
            return null;
        }

        // Azuriranje vrednosti sredstava
        sredstvaKapital.setUkupno(novoStanje);
        sredstvaKapital.setRezervisano(novoRezervisano);
        sredstvaKapital.setRaspolozivo(novoRaspolozivo);

        // Pravljenje transakcije
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

        // Racunanje i izmena limita
        // Konverzija iz ne-RSD valutu u RSD
        if(limitDelta != 0) {
            if (!kodValute.equalsIgnoreCase("RSD")) {
                ResponseEntity<ForexPodaciDto> resp = HttpUtils.getExchangeRate(FOREX_EXCHANGE_RATE_URL, token, kodValute, "RSD");
                if (resp.getBody() == null) {
                    return null;
                }
                ForexPodaciDto fpd = resp.getBody();
                limitDelta *= fpd.getExchangeRate();
            }
            // Poziv user servisu da azurira limit
            HttpUtils.updateUserLimit(USER_SERVICE_URL, token, limitDelta);
        }

        // Cuvanje podataka
        t = transakcijaRepository.save(t);
        sredstvaKapitalRepository.save(sredstvaKapital);

        return t;
    }

}
