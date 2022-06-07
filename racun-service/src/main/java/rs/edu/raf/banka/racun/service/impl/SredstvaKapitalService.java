package rs.edu.raf.banka.racun.service.impl;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.*;

import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.time.Duration;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final RacunRepository racunRepository;
    private final ValutaRepository valutaRepository;
    private final WebClient webClient;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository, RacunRepository racunRepository, ValutaRepository valutaRepository,WebClient webClient) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
        this.webClient = webClient;
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

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
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

    public KapitalStanje getSumStanje(List<SredstvaKapital> kapitali)
    {
        var rates = new HashMap<String, Double>();
        rates.put("RSD", 1.0);
        HashMap<KapitalType, Double> values = new HashMap<>();

        for(var kapitalType: KapitalType.values())
            values.put(kapitalType, 0.0);

        for(var kapital: kapitali)
        {
            var valuta = kapital.getValuta().getKodValute();
            if(!rates.containsKey(valuta))
            {
                var rate = getRSDForexRate(valuta);
                rates.put(valuta, rate.getExchangeRate());
            }
            double value = kapital.getUkupno() * rates.get(valuta);
            values.compute(kapital.getKapitalType(), (k,v) -> v + value);
        }

        KapitalStanje result = new KapitalStanje();
        result.setNovac(values.get(KapitalType.NOVAC));
        result.setForex(values.get(KapitalType.FOREX));
        result.setFuture(values.get(KapitalType.FUTURE_UGOVOR));
        result.setAkcija(values.get(KapitalType.AKCIJA));

        double sum = result.getNovac() + result.getAkcija() + result.getForex() + result.getFuture();
        result.setUkupno(sum);
        return result;
    }

    public ForexPodaciDto getRSDForexRate(String symbolFrom) {
        HashMap<String, List<String>> req = new HashMap<>();

        return webClient
                .post()
                .uri("/api/forex/podaci/" + symbolFrom + "/RSD")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ForexPodaciDto>() {})
                .block(REQUEST_TIMEOUT);
    }
}
