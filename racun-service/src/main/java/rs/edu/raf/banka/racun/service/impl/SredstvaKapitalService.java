package rs.edu.raf.banka.racun.service.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.ForexPodaciDto;
import rs.edu.raf.banka.racun.dto.KapitalStanjeDto;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.*;
import rs.edu.raf.banka.racun.dto.AgentSredstvaKapitalDto;
import rs.edu.raf.banka.racun.dto.SredstvaKapitalDto;

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

        System.err.println("OVDE 3");
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        sredstvaKapital.setKapitalType(kapitalType);
        sredstvaKapital.setHaritjeOdVrednostiID(hartijaId);
        System.err.println("OVDE 4");

        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

    public KapitalStanjeDto getSumStanje(List<SredstvaKapital> kapitali, String token)
    {
        var rates = new HashMap<String, Double>();
        rates.put("RSD", 1.0);
        HashMap<KapitalType, Double> values = new HashMap<>();

        for(var kapitalType: KapitalType.values())
            values.put(kapitalType, 0.0);

        for(var kapital: kapitali)
        {
            var valuta = kapital.getValuta().getKodValute();
            if(!rates.containsKey(valuta))  {
                ResponseEntity<ForexPodaciDto> resp = HttpUtils.getExchangeRate(FOREX_EXCHANGE_RATE_URL, token, valuta, "RSD");
                if (resp.getBody() == null) {
                    return null;
                }

                //var rate = getRSDForexRate(valuta);
                //rates.put(valuta, rate.getExchangeRate());
            }
            double value = kapital.getUkupno() * rates.get(valuta);
            values.compute(kapital.getKapitalType(), (k,v) -> v + value);
        }

        KapitalStanjeDto result = new KapitalStanjeDto();
        result.setNovac(values.get(KapitalType.NOVAC));
        result.setForex(values.get(KapitalType.FOREX));
        result.setFuture(values.get(KapitalType.FUTURE_UGOVOR));
        result.setAkcija(values.get(KapitalType.AKCIJA));

        double sum = result.getNovac() + result.getAkcija() + result.getForex() + result.getFuture();
        result.setUkupno(sum);
        return result;
    }

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
