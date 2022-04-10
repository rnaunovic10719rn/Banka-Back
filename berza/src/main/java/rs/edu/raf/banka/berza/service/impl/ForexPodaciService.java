package rs.edu.raf.banka.berza.service.impl;

import com.crazzyghost.alphavantage.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.request.ForexExchangeRequest;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.repository.ForexRepository;

import java.time.Duration;
import java.util.*;

@Service
public class ForexPodaciService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private ForexRepository forexRepository;

    private final WebClient influxApiClient;
    private final Config alphavantageApiClient;

    private final List<String> odabaraniParovi = Arrays.asList("EUR/USD", "EUR/RSD", "USD/RSD");

    @Autowired
    public ForexPodaciService(ForexRepository forexRepository,
                              WebClient influxApiClient,
                              Config alphavantageApiClient){
        this.forexRepository = forexRepository;
        this.influxApiClient = influxApiClient;
        this.alphavantageApiClient = alphavantageApiClient;
    }

    public List<ForexPodaciDto> getOdabraniParovi() {
        List<ForexPodaciDto> fpd = new ArrayList<>();
        for(String p: odabaraniParovi) {
            String[] ps = p.split("/");
            if(ps.length != 2) {
                continue;
            }
            fpd.add(getForexBySymbol(ps[0], ps[1]));
        }
        return fpd;
    }

    public ForexPodaciDto getForexBySymbol(String symbolFrom, String symbolTo) {
        List<ForexExchangeRequest> reqs = new ArrayList<>();
        ForexExchangeRequest fer = new ForexExchangeRequest();
        fer.setSymbolFrom(symbolFrom);
        fer.setSymbolTo(symbolTo);
        reqs.add(fer);

        HashMap<String, List<ForexExchangeRequest>> req = new HashMap<>();
        req.put("currencies", reqs);

        List<ForexPodaciDto> dtoList = influxApiClient
                .post()
                .uri("alphavantage/forex/exchangerate/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ForexPodaciDto>>() {})
                .block(REQUEST_TIMEOUT);
        if(dtoList == null || dtoList.size() == 0) {
            return null;
        }

        return dtoList.get(0);
    }

    public List<Forex> getAllForex(){
        return forexRepository.findAll();
    }

//    public Page<Forex> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
//        Forex forex = new Forex();
//        forex.setOznakaHartije(oznakaHartije);
//        forex.setOpis_hartije(opisHartije);
//
//        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
//                .withMatcher("oznaka_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
//                .withMatcher("opis_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
//        Example<Forex> example = Example.of(forex, exampleMatcher);
//
//        return forexRepository.findAll(example, PageRequest.of(page, size));
//    }
//
//    public Page<Forex> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
//                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
//        List<Forex> forexList = forexRepository.filterForex(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
//        return new PageImpl<Forex>(forexList, PageRequest.of(page, size), forexList.size());
//    }
//
//    public Forex getByID(Long id){
//        return forexRepository.findForexById(id);
//    }

}
