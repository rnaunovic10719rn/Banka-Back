package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.ForexPodaciDto;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.utils.HttpUtils;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

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

    @Value("${racun.user-service-baseurl}")
    private String USER_SERVICE_BASE_URL;

    @Value("${racun.berza-service-baseurl}")
    private String BERZA_SERVICE_BASE_URL;

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
        String username = userService.getUsernameByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username);
        else
            return transakcijaRepository.getAll();
    }

    public List<Transakcija> getAll(String token, Date odFilter, Date doFilter){
        String role = userService.getRoleByToken(token);
        String username = userService.getUsernameByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username, odFilter, doFilter);
        else
            return transakcijaRepository.getAll(odFilter, doFilter);
    }

    public List<Transakcija> getAll(String token, String valuta){
        String role = userService.getRoleByToken(token);
        String username = userService.getUsernameByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username, valuta);
        else
            return transakcijaRepository.getAll(valuta);
    }

    public List<Transakcija> getAll(String token, String valuta, Date odFilter, Date doFilter){
        String role = userService.getRoleByToken(token);
        String username = userService.getUsernameByToken(token);
        if (role.equals("ROLE_AGENT"))
            return transakcijaRepository.findByUsername(username, valuta, odFilter, doFilter);
        else
            return transakcijaRepository.getAll(valuta, odFilter, doFilter);
    }

    @Transactional
    public Transakcija dodajTransakciju(String token, TransakcijaRequest transakcijaRequest){
        String username;
        if(token.equals("Bearer BERZA-SERVICE")) {
            username = transakcijaRequest.getUsername();
        } else {
            username = userService.getUsernameByToken(token);
        }

        // KORAK 1: Uzmi objekat Racuna i Valute.
        RacunType racunType = RacunType.KES;
        if(transakcijaRequest.isMargins()) {
            racunType = RacunType.MARGINS_RACUN;
        }
        Racun racun = racunRepository.findRacunByTipRacuna(racunType);
        if (racun == null) {
            log.error("dodajTransakciju: failed to get racun");
            return null;
        }
        Valuta valuta = null;
        if(transakcijaRequest.getValutaOznaka() != null && !transakcijaRequest.getValutaOznaka().isBlank()) {
            valuta = valutaRepository.findValutaByKodValute(transakcijaRequest.getValutaOznaka());
            if (valuta == null) {
                log.error("dodajTransakciju: failed to get valuta {}", transakcijaRequest.getValutaOznaka());
                return null;
            }
        }

        if(transakcijaRequest.getType() == KapitalType.NOVAC) {
            SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacunAndValuta(racun, valuta);
            if (sredstvaKapital == null) {
                sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), transakcijaRequest.getValutaOznaka(), 0);
            }
        } else {
            SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacunAndHaritja(racun, transakcijaRequest.getType(), transakcijaRequest.getHartijaId());
            if (sredstvaKapital == null) {
                sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), transakcijaRequest.getType(), transakcijaRequest.getHartijaId(), 0);
            }
        }

        Query query;
        if(transakcijaRequest.getType() == KapitalType.NOVAC) {
            query = entityManager.createQuery("from SredstvaKapital where racun = :racun and valuta = :valuta and kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.NOVAC");
            query.setParameter("valuta", valuta);
        } else {
            query = entityManager.createQuery("from SredstvaKapital where racun = :racun and haritjeOdVrednostiID = :hartijaId and kapitalType = :kapitalType");
            query.setParameter("hartijaId", transakcijaRequest.getHartijaId());
            query.setParameter("kapitalType", transakcijaRequest.getType());
        }
        query.setParameter("racun", racun);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<SredstvaKapital> skList = query.getResultList();
        if(skList.size() != 1) {
            log.error("dodajTransakciju: unable to find sredstvaKapital for {} and {}", racun.getBrojRacuna().toString(), transakcijaRequest.getValutaOznaka());
            return null;
        }

        SredstvaKapital sredstvaKapital = skList.get(0);

        // Izracunaj novo ukupno stanje na racunu
        Double novoStanje = sredstvaKapital.getUkupno() + transakcijaRequest.getUplata() - transakcijaRequest.getIsplata();

        // Izracunaj koliko se rezervisanih sredstava koristi u transakciji
        Double rezervisanoKoristi = 0.0;
        // Pokupi koliko je preostalo rezervisanih sredstava za taj order
        Double rezervisanoForOrder = transakcijaRepository.getRezervisanoForOrder(transakcijaRequest.getOrderId());
        if (rezervisanoForOrder != null) {
            if(rezervisanoForOrder > transakcijaRequest.getIsplata()) {
                rezervisanoKoristi = transakcijaRequest.getIsplata();
            } else {
                rezervisanoKoristi = rezervisanoForOrder;
            }
        }

        // Ukoliko je poslednji segmet, proveri da li treba izvrsiti povracaj rezervisanih sredstava, u slucaju da je ostao visak
        Double rezervisano = transakcijaRequest.getRezervisano();
        if (transakcijaRequest.getLastSegment() && rezervisanoForOrder != null && rezervisanoForOrder + rezervisano - rezervisanoKoristi > 0) {
            rezervisano += -1 * (rezervisanoForOrder - rezervisanoKoristi);
        }

        // Izracunaj novo rezervisano i raspolozivo
        Double novoRezervisano = sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi;
        Double novoRaspolozivo = novoStanje - novoRezervisano;

        // Izracunaj promenu limita korisnika
        Double limitDelta = rezervisano + (transakcijaRequest.getIsplata()-rezervisanoKoristi);

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
        t.setKapitalType(transakcijaRequest.getType());
        if(transakcijaRequest.getType() == KapitalType.NOVAC) {
            t.setValuta(valuta);
        } else {
            t.setHaritjeOdVrednostiID(transakcijaRequest.getHartijaId());
        }
        t.setOrderId(transakcijaRequest.getOrderId());
        t.setDatumVreme(new Date());
        t.setOpis(transakcijaRequest.getOpis());
        t.setUplata(transakcijaRequest.getUplata());
        t.setIsplata(transakcijaRequest.getIsplata());
        t.setRezervisano(rezervisano);
        t.setRezervisanoKoristi(rezervisanoKoristi);
        t.setUnitPrice(transakcijaRequest.getUnitPrice());

        // Racunanje i izmena limita
        // Konverzija iz ne-RSD valutu u RSD
        if(transakcijaRequest.getType() == KapitalType.NOVAC && limitDelta != 0 && !token.equals("Bearer BERZA-SERVICE")) {
            if (!transakcijaRequest.getValutaOznaka().equalsIgnoreCase("RSD")) {
                ResponseEntity<ForexPodaciDto> resp = HttpUtils.getExchangeRate(BERZA_SERVICE_BASE_URL, token, transakcijaRequest.getValutaOznaka(), "RSD");
                if (resp.getBody() == null) {
                    return null;
                }
                ForexPodaciDto fpd = resp.getBody();
                limitDelta *= fpd.getExchangeRate();
            }
            // Poziv user servisu da azurira limit
            HttpUtils.updateUserLimit(USER_SERVICE_BASE_URL, token, limitDelta);
        }

        // Cuvanje podataka
        t = transakcijaRepository.save(t);
        sredstvaKapitalRepository.save(sredstvaKapital);

        return t;
    }

}
