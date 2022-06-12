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
import java.util.HashMap;
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

    public SredstvaKapital get(UUID racun, String valuta, KapitalType hartijaType, Long hartijaId) {
        return sredstvaKapitalRepository.findByRacunAndValutaAndHaritja(racunRepository.findByBrojRacuna(racun), valutaRepository.findValutaByKodValute(valuta),hartijaType, hartijaId);
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

    public SredstvaKapital pocetnoStanje(UUID uuidRacuna, String kodValute, Long hartijaId, double ukupno) {
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }

        Valuta valuta = valutaRepository.findValutaByKodValute(kodValute);
        if(valuta == null) {
            return null;
        }

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setValuta(valuta);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        sredstvaKapital.setKapitalType(KapitalType.NOVAC);
        sredstvaKapital.setHaritjeOdVrednostiID(hartijaId);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

//    public List<KapitalHartijeDto> getUkupnoStanjePoHartijama(UUID racun) {
//        List<SredstvaKapital> sredstvaKapitals = this.getAll(racun);
//        List<KapitalHartijeDto> toReturn = new ArrayList<>();
//        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
//            if (sredstvaKapital.getHaritjeOdVrednostiID() != null) {
//                if (sredstvaKapital.getKapitalType().equals(KapitalType.AKCIJA)) {
//                    for (KapitalHartijeDto khd : toReturn) {
//                        if (khd.getKapitalType().equals(KapitalType.AKCIJA)) {
//
//                            khd.setUkupno(khd.getUkupno());
//                        }
//                    }
//                }
//            }
//        }
//    }


    public ResponseEntity<FuturesPodaciDto> getFuture(Long id) {
        return HttpUtils.getFuturesById(FUTURES_BY_ID_URL, id);
    }
    public ResponseEntity<AkcijePodaciDto> getAkcija(Long id) {
        return HttpUtils.getAkcijeById(AKCIJE_BY_ID_URL, id);
    }

//    public KapitalHartijeDto getSumStanje(List<SredstvaKapital> kapitali, String token)
//    {
//        var rates = new HashMap<String, Double>();
//        rates.put("RSD", 1.0);
//        HashMap<KapitalType, Double> values = new HashMap<>();
//
//        for(var kapitalType: KapitalType.values())
//            values.put(kapitalType, 0.0);
//
//        for(var kapital: kapitali)
//        {
//            var valuta = kapital.getValuta().getKodValute();
//            if(!rates.containsKey(valuta))  {
//                ResponseEntity<ForexPodaciDto> resp = HttpUtils.getExchangeRate(FOREX_EXCHANGE_RATE_URL, token, valuta, "RSD");
//                if (resp.getBody() == null) {
//                    return null;
//                }
//
//                //var rate = getRSDForexRate(valuta);
//                //rates.put(valuta, rate.getExchangeRate());
//            }
//            double value = kapital.getUkupno() * rates.get(valuta);
//            values.compute(kapital.getKapitalType(), (k,v) -> v + value);
//        }
//
//        KapitalHartijeDto result = new KapitalHartijeDto();
//        result.setNovac(values.get(KapitalType.NOVAC));
//        result.setForex(values.get(KapitalType.FOREX));
//        result.setFuture(values.get(KapitalType.FUTURE_UGOVOR));
//        result.setAkcija(values.get(KapitalType.AKCIJA));
//
//        double sum = result.getNovac() + result.getAkcija() + result.getForex() + result.getFuture();
//        result.setUkupno(sum);
//        return result;
//    }

    public List<SredstvaKapitalDto> findSredstvaKapitalSupervisor(String token) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT"))
            return null;
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAll();
        List<SredstvaKapitalDto> sredstvaKapitalDtos = new ArrayList<>();
        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            SredstvaKapitalDto s = new SredstvaKapitalDto();
            s.setKodValute(sredstvaKapital.getValuta().getKodValute());
            s.setUkupno(sredstvaKapital.getUkupno());
            s.setRezervisano(sredstvaKapital.getRezervisano());
            s.setRaspolozivo(sredstvaKapital.getRaspolozivo());
            sredstvaKapitalDtos.add(s);
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
