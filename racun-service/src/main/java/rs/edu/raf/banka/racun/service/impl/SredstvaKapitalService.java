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
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;

    private final TransakcijaRepository transakcijaRepository;
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

    @Value("${racun.berzaById-url}")
    private String BERZA_BY_ID_URL;

    @Autowired
    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository,
                                  TransakcijaRepository transakcijaRepository,
                                  RacunRepository racunRepository,
                                  ValutaRepository valutaRepository,
                                  UserService userService) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.transakcijaRepository = transakcijaRepository;
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

                BerzaDto berzaDto = this.getBerza(akcijePodaciDto.getBerzaId());
                ForexPodaciDto forexPodaciDto = this.getForex(token, berzaDto.getKodValute());

                Double cenaTrenutneHartije = sredstvaKapital.getRaspolozivo() * akcijePodaciDto.getPrice() * forexPodaciDto.getExchangeRate();
                khdAkcija.setUkupno(khdAkcija.getUkupno() + cenaTrenutneHartije);
            }

            if (sredstvaKapital.getKapitalType().equals(KapitalType.FUTURE_UGOVOR)) {

                FuturesPodaciDto futuresPodaciDto = this.getFuture(sredstvaKapital.getHaritjeOdVrednostiID());

                ForexPodaciDto forexPodaciDto = this.getForex(token,"USD");

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
                kapitalPoTipuHartijeDto.setId(akcijePodaciDto.getId());
                kapitalPoTipuHartijeDto.setOznakaHartije(akcijePodaciDto.getTicker());
                BerzaDto berzaDto = this.getBerza(akcijePodaciDto.getBerzaId());
                kapitalPoTipuHartijeDto.setBerza(berzaDto.getOznakaBerze());
                Long kolicinaUVlasnistvu = (long) sredstvaKapital.getUkupno();
                kapitalPoTipuHartijeDto.setKolicinaUVlasnistvu(kolicinaUVlasnistvu);
                Double cena = akcijePodaciDto.getPrice();
                kapitalPoTipuHartijeDto.setCena(cena);
                Double vrednost = cena*kolicinaUVlasnistvu;
                kapitalPoTipuHartijeDto.setVrednost(vrednost);
                Double kupljenoZa = transakcijaRepository.getKupljenoZa(sredstvaKapital.getHaritjeOdVrednostiID());
                kapitalPoTipuHartijeDto.setKupljenoZa(kupljenoZa);
                Double profit = cena - kupljenoZa;
                kapitalPoTipuHartijeDto.setProfit(profit);
                toReturn.add(kapitalPoTipuHartijeDto);
            }
            if (kapitalType.equals(KapitalType.FUTURE_UGOVOR.toString())) {
                FuturesPodaciDto futuresPodaciDto = this.getFuture(sredstvaKapital.getHaritjeOdVrednostiID());
                KapitalPoTipuHartijeDto kapitalPoTipuHartijeDto = new KapitalPoTipuHartijeDto();
                kapitalPoTipuHartijeDto.setId(futuresPodaciDto.getId());
                kapitalPoTipuHartijeDto.setOznakaHartije(futuresPodaciDto.getSymbol());
                kapitalPoTipuHartijeDto.setBerza("EUREX");
                Long kolicinaUVlasnistvu = (long) sredstvaKapital.getUkupno();
                kapitalPoTipuHartijeDto.setKolicinaUVlasnistvu(kolicinaUVlasnistvu);
                Double cena = futuresPodaciDto.getOpen();
                kapitalPoTipuHartijeDto.setCena(cena);
                Double vrednost = cena*kolicinaUVlasnistvu;
                kapitalPoTipuHartijeDto.setVrednost(vrednost);
                Double kupljenoZa = transakcijaRepository.getKupljenoZa(sredstvaKapital.getHaritjeOdVrednostiID());
                kapitalPoTipuHartijeDto.setKupljenoZa(kupljenoZa);
                Double profit = cena - kupljenoZa;
                kapitalPoTipuHartijeDto.setProfit(profit);
                toReturn.add(kapitalPoTipuHartijeDto);
            }
        }
        return toReturn;
    }

    public List<TransakcijeHartijeDto> getTransakcijeHartije(Long id) {
        List<Transakcija> transakcijaList = transakcijaRepository.findByHaritjeOdVrednostiID(id);
        List<TransakcijeHartijeDto> toReturn = new ArrayList<>();
        for (Transakcija transakcija : transakcijaList) {
            TransakcijeHartijeDto transakcijeHartijeDto = new TransakcijeHartijeDto();
            transakcijeHartijeDto.setDatum(transakcija.getDatumVreme());
            if (transakcija.getUplata() > 0) {
                transakcijeHartijeDto.setTipOrdera("Kupovina");
                transakcijeHartijeDto.setKolicina((long) transakcija.getUplata());
            }
            else if (transakcija.getIsplata() > 0) {
                transakcijeHartijeDto.setTipOrdera("Prodaja");
                transakcijeHartijeDto.setKolicina((long) transakcija.getIsplata());
            }
            transakcijeHartijeDto.setCena(transakcija.getUnitPrice());
            transakcijeHartijeDto.setUkupno(transakcija.getUnitPrice()*transakcijeHartijeDto.getKolicina());
            toReturn.add(transakcijeHartijeDto);
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

    public BerzaDto getBerza(Long id) {
        ResponseEntity<BerzaDto> berzaResp = HttpUtils.getBerzaById(BERZA_BY_ID_URL, id);
        if (berzaResp.getBody() == null) {
            return null;
        }
        BerzaDto berzaDto = berzaResp.getBody();
        return berzaDto;
    }

    public ForexPodaciDto getForex(String token, String from) {
        ResponseEntity<ForexPodaciDto> fpdResp = HttpUtils.getExchangeRate(FOREX_EXCHANGE_RATE_URL, token, from, "RSD");
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
