package rs.edu.raf.banka.racun.service.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.*;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.*;

import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final RacunRepository racunRepository;
    private final ValutaRepository valutaRepository;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private final UserService userService;

    @Value("${racun.forex-quote-url}")
    private String FOREX_EXCHANGE_RATE_URL;

    @Value("${racun.akcije-quote-url}")
    private String AKCIJE_BY_ID_URL;

    @Value("${racun.futures-quote-url}")
    private String FUTURES_BY_ID_URL;

    @Autowired
    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository,
                                  RacunRepository racunRepository,
                                  ValutaRepository valutaRepository,
                                  UserService userService) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
        this.userService = userService;
    }

    public List<SredstvaKapital> getAll(UUID racun) {
        return sredstvaKapitalRepository.findAllByRacun(racunRepository.findByBrojRacuna(racun));
    }

    public SredstvaKapital get(UUID racun, String valuta) {
        return sredstvaKapitalRepository.findByRacunAndValuta(racunRepository.findByBrojRacuna(racun), valutaRepository.findValutaByKodValute(valuta));
    }

    public SredstvaKapital get(UUID racun, KapitalType hartijaType, Long hartijaId) {
        return sredstvaKapitalRepository.findByRacunAndHaritja(racunRepository.findByBrojRacuna(racun), hartijaType, hartijaId);
    }

    public SredstvaKapital pocetnoStanje(UUID uuidRacuna, String kodValute, double ukupno) {
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }
        Valuta valuta = valutaRepository.findValutaByKodValute(kodValute);
        if(valuta == null) {
            return null;
        }

        SredstvaKapital sredstvaKapital;
        sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setValuta(valuta);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        sredstvaKapital.setKapitalType(KapitalType.NOVAC);
        sredstvaKapital.setHaritjeOdVrednostiID(-1L);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

    public SredstvaKapital pocetnoStanje(UUID uuidRacuna, KapitalType kapitalType, Long hartijaId, double ukupno) {
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        sredstvaKapital.setKapitalType(kapitalType);
        sredstvaKapital.setHaritjeOdVrednostiID(hartijaId);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

    public List<KapitalHartijeDto> getUkupnoStanjePoHartijama(String token) {
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAll();
        List<KapitalHartijeDto> toReturn = new ArrayList<>();
        KapitalHartijeDto khdAkcija = new KapitalHartijeDto(KapitalType.AKCIJA, 0.0);
        KapitalHartijeDto khdFuture = new KapitalHartijeDto(KapitalType.FUTURE_UGOVOR, 0.0);
        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            //TODO: ovo treba kasnije prosiriti na opcije i obaveznice
            if (sredstvaKapital.getKapitalType().equals(KapitalType.AKCIJA)) {

                AkcijePodaciDto akcijePodaciDto = this.getAkcija(sredstvaKapital.getHaritjeOdVrednostiID());

                //TODO: iz berze dohvatiti valutu za hartiju pa onda za tu valutu racunati rate, za sada se pretpostavlja da je dolar
                ForexPodaciDto forexPodaciDto = this.getForex(token);

                Double cenaTrenutneHartije = sredstvaKapital.getRaspolozivo() * akcijePodaciDto.getPrice() * forexPodaciDto.getExchangeRate();
                khdAkcija.setUkupno(khdAkcija.getUkupno() + cenaTrenutneHartije);
            }

            if (sredstvaKapital.getKapitalType().equals(KapitalType.FUTURE_UGOVOR)) {

                FuturesPodaciDto futuresPodaciDto = this.getFuture(sredstvaKapital.getHaritjeOdVrednostiID());

                //TODO: iz berze dohvatiti valutu za hartiju pa onda za tu valutu racunati rate, za sada se pretpostavlja da je dolar
                ForexPodaciDto forexPodaciDto = this.getForex(token);

                Double cenaTrenutneHartije = sredstvaKapital.getRaspolozivo() * futuresPodaciDto.getOpen() * forexPodaciDto.getExchangeRate();
                khdAkcija.setUkupno(khdAkcija.getUkupno() + cenaTrenutneHartije);
            }
        }
        toReturn.add(khdAkcija);
        toReturn.add(khdFuture);
        return toReturn;
    }

    public List<KapitalPoTipuHartijeDto> getStanjeJednogTipaHartije(String token, String kapitalType) {
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAll();
        List<KapitalPoTipuHartijeDto> toReturn = new ArrayList<>();
        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            //TODO: treba dopuniti sa opcijama i obaveznicama
            if (sredstvaKapital.getKapitalType().equals(KapitalType.NOVAC))
                continue;
            if (kapitalType.equals(KapitalType.AKCIJA.toString())) {
                AkcijePodaciDto akcijePodaciDto = this.getAkcija(sredstvaKapital.getHaritjeOdVrednostiID());
                KapitalPoTipuHartijeDto kapitalPoTipuHartijeDto = new KapitalPoTipuHartijeDto();
                kapitalPoTipuHartijeDto.setOznakaHartije(akcijePodaciDto.getTicker());
                kapitalPoTipuHartijeDto.setOpisHartije(akcijePodaciDto.getOpisHartije());
                kapitalPoTipuHartijeDto.setIdBerza(akcijePodaciDto.getBerzaId());
                Long kolicinaUVlasnistvu = (long) sredstvaKapital.getUkupno();
                kapitalPoTipuHartijeDto.setKolicinaUVlasnistvu(kolicinaUVlasnistvu);
                Double cena = akcijePodaciDto.getPrice();
                kapitalPoTipuHartijeDto.setCena(cena);
                Double vrednost = cena*kolicinaUVlasnistvu;
                kapitalPoTipuHartijeDto.setVrednost(vrednost);
                Double kupljenoZa = 0.0; //treba izmeniti
                kapitalPoTipuHartijeDto.setKupljenoZa(kupljenoZa);
                Double profit = 0.0; //treba izmeniti
                kapitalPoTipuHartijeDto.setProfit(profit);
                toReturn.add(kapitalPoTipuHartijeDto);
            }
            if (kapitalType.equals(KapitalType.FUTURE_UGOVOR.toString())) {
                FuturesPodaciDto futuresPodaciDto = this.getFuture(sredstvaKapital.getHaritjeOdVrednostiID());
                KapitalPoTipuHartijeDto kapitalPoTipuHartijeDto = new KapitalPoTipuHartijeDto();
                kapitalPoTipuHartijeDto.setOznakaHartije(futuresPodaciDto.getSymbol());
                //kapitalPoTipuHartijeDto.setOpisHartije(futuresPodaciDto.get);
                //kapitalPoTipuHartijeDto.setIdBerza(futuresPodaciDto.get);
                Long kolicinaUVlasnistvu = (long) sredstvaKapital.getUkupno();
                kapitalPoTipuHartijeDto.setKolicinaUVlasnistvu(kolicinaUVlasnistvu);
                Double cena = futuresPodaciDto.getOpen();
                kapitalPoTipuHartijeDto.setCena(cena);
                Double vrednost = cena*kolicinaUVlasnistvu;
                kapitalPoTipuHartijeDto.setVrednost(vrednost);
                Double kupljenoZa = 0.0; //treba izmeniti
                kapitalPoTipuHartijeDto.setKupljenoZa(kupljenoZa);
                Double profit = 0.0; //treba izmeniti
                kapitalPoTipuHartijeDto.setProfit(profit);
                toReturn.add(kapitalPoTipuHartijeDto);
            }
        }
        return toReturn;
    }

    public AkcijePodaciDto getAkcija(Long id) {
        ResponseEntity<AkcijePodaciDto> apdResp = HttpUtils.getAkcijeById(AKCIJE_BY_ID_URL, id);
        if (apdResp.getBody() == null) {
            return null;
        }
        AkcijePodaciDto akcijePodaciDto = apdResp.getBody();
        return akcijePodaciDto;
    }

    public ForexPodaciDto getForex(String token) {
        ResponseEntity<ForexPodaciDto> fpdResp = HttpUtils.getExchangeRate(FOREX_EXCHANGE_RATE_URL, token, "USD", "RSD");
        if (fpdResp.getBody() == null) {
            return null;
        }
        ForexPodaciDto forexPodaciDto = fpdResp.getBody();
        return forexPodaciDto;
    }

    public FuturesPodaciDto getFuture(Long id) {
        ResponseEntity<FuturesPodaciDto> futureResp = HttpUtils.getFuturesById(FUTURES_BY_ID_URL, id);
        if (futureResp.getBody() == null) {
            return null;
        }
        FuturesPodaciDto futuresPodaciDto = futureResp.getBody();
        return futuresPodaciDto;
    }


    public List<SupervisorSredstvaKapitalDto> findSredstvaKapitalSupervisor(String token) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT"))
            return null;
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAll();
        List<SupervisorSredstvaKapitalDto> sredstvaKapitalDtos = new ArrayList<>();
        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            if (sredstvaKapital.getKapitalType().equals(KapitalType.NOVAC)) {
                SupervisorSredstvaKapitalDto s = new SupervisorSredstvaKapitalDto();
                s.setKodValute(sredstvaKapital.getValuta().getKodValute());
                s.setUkupno(sredstvaKapital.getUkupno());
                s.setRezervisano(sredstvaKapital.getRezervisano());
                s.setRaspolozivo(sredstvaKapital.getRaspolozivo());
                sredstvaKapitalDtos.add(s);
            }
        }
        return sredstvaKapitalDtos;
    }

    public AgentSredstvaKapitalDto findSredstvaKapitalAgent(String token) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT")) {
            AgentSredstvaKapitalDto agentSredstvaKapitalDto = new AgentSredstvaKapitalDto();
            agentSredstvaKapitalDto.setLimit(userService.getUserByToken(token).getLimit());
            agentSredstvaKapitalDto.setLimitUsed(userService.getUserByToken(token).getLimitUsed());
            agentSredstvaKapitalDto.setRaspolozivoAgentu(agentSredstvaKapitalDto.getLimit() - agentSredstvaKapitalDto.getLimitUsed());
            return agentSredstvaKapitalDto;
        }
           return null;
    }
}
