package rs.edu.raf.banka.berza.service.impl;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverview;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class AkcijePodaciService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient influxApiClient;
    private final Config alphavantageApiClient;

    private AkcijeRepository akcijeRepository;

    private final List<String> odabraneAkcije = Arrays.asList("AAPL", "MSFT", "GOOG", "BA", "AXP");

    @Autowired
    public AkcijePodaciService(AkcijeRepository akcijeRepository,
                               WebClient influxApiClient,
                               Config alphavantageApiClient){
        this.akcijeRepository = akcijeRepository;
        this.influxApiClient = influxApiClient;
        this.alphavantageApiClient = alphavantageApiClient;
    }

    public List<AkcijePodaciDto> getOdabraneAkcije() {
//        AkcijePodaciRequest req = new AkcijePodaciRequest(odabraneAkcije);

        return null;
//        return influxApiClient
//                .post()
//                .uri("/test/")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromObject(req))
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<AkcijePodaciDto>>() {})
//                .block(REQUEST_TIMEOUT);
    }

    public AkcijePodaciDto getAkcijaByTicker(String ticker) {
        Akcije akcija = akcijeRepository.findAkcijeByOznakaHartije(ticker);
        // TODO: Dodati neki data decline, kada će informacije da se refreshuju, npr. posle dva dana.
        if (akcija == null) {
            akcija = new Akcije();
            CompanyOverviewResponse cor = AlphaVantage
                    .api()
                    .fundamentalData()
                    .companyOverview()
                    .forSymbol(ticker)
                    .fetchSync();
            if(cor != null && cor.getErrorMessage() == null) {
                CompanyOverview co = cor.getOverview();
                akcija.setOznakaHartije(co.getSymbol());
                akcija.setOznakaHartije("");
                akcija.setOpis_hartije(co.getName());
                akcija.setLast_updated(new Date());
                akcija.setOutstanding_shares(co.getSharesOutstanding());
            } else {
                // Nećemo da pucamo u slučaju da se desila API greška.
                // API je rate limitovan, pa bi to dosta kočilo.
                // U produkciji svakako treba izbaciti grešku.
                akcija.setOznakaHartije(ticker);
                akcija.setOpis_hartije(ticker);
                akcija.setLast_updated(new Date());
                akcija.setOutstanding_shares(0L);
            }
            akcijeRepository.save(akcija);
        }

        AkcijePodaciDto dto = new AkcijePodaciDto();
        dto.setTicker(ticker);
        dto.setOpisHartije(akcija.getOpis_hartije());
        dto.setOpen(0.0);
        dto.setClose(0.0);
        dto.setLow(0.0);
        dto.setHigh(0.0);
        dto.setOutstandingShares(akcija.getOutstanding_shares());

        return dto;


//        influxApiClient
//                .post()
//                .uri("/alphavantage/")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromObject(req))
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<AkcijePodaciDto>>() {})
//                .block(REQUEST_TIMEOUT);

//        AkcijePodaciRequest req = new AkcijePodaciRequest(odabraneAkcije);
//
//        return influxApiClient
//                .post()
//                .uri("/test/")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromObject(req))
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<AkcijePodaciDto>>() {})
//                .block(REQUEST_TIMEOUT);
    }

    public List<Akcije> getAllAkcije(){
        return akcijeRepository.findAll();
    }

    public Page<Akcije> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
        Akcije akcije = new Akcije();
        akcije.setOznakaHartije(oznakaHartije);
        akcije.setOpis_hartije(opisHartije);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznaka_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opis_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Akcije> example = Example.of(akcije, exampleMatcher);

        return akcijeRepository.findAll(example, PageRequest.of(page, size));
    }

    public Page<Akcije> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
        List<Akcije> akcije = akcijeRepository.filterAkcije(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
        return new PageImpl<Akcije>(akcije, PageRequest.of(page, size), akcije.size());
    }

    public Akcije getByID(Long id){
        return akcijeRepository.findAkcijeById(id);
    }
}
