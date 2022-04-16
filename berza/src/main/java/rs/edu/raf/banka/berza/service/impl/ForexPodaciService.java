package rs.edu.raf.banka.berza.service.impl;

import com.crazzyghost.alphavantage.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.ForexTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.*;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.repository.ForexRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

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

    public List<ForexTimeseriesDto> getForexTimeseries(ForexTimeseriesUpdateRequest req) {
        influxApiClient
                .post()
                .uri("/alphavantage/forex/updatewait/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AkcijePodaciDto>>() {})
                .block(REQUEST_TIMEOUT);

        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String endDate = zonedDateTime.format(endFormatter);

        if(req.getType().equals("intraday") && req.getInterval().equals("5min")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    break;
                case MONDAY:
                    if (zonedDateTime.getHour() < 16) {
                        zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    }
                    break;
            }
        } else if(req.getType().equals("intraday") && req.getInterval().equals("30min")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    break;
                case MONDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                    break;
                default:
                    zonedDateTime = zonedDateTime.minusDays(7); // 7 zbog vikenda
            }
        } else {
            switch (req.getRequestType()) {
                case "1m":
                    zonedDateTime = zonedDateTime.minusMonths(1);
                    break;
                case "6m":
                    zonedDateTime = zonedDateTime.minusMonths(6);
                    break;
                case "1y":
                    zonedDateTime = zonedDateTime.minusMonths(12);
                    break;
                case "2y":
                    zonedDateTime = zonedDateTime.minusMonths(24);
                    break;
                case "ytd":
                    zonedDateTime = zonedDateTime.with(firstDayOfYear());
                    break;
            }
        }

        String startDate = zonedDateTime.format(startFormatter);

        ForexTimeseriesReadRequest readReq = new ForexTimeseriesReadRequest();
        readReq.setType(req.getType());
        readReq.setSymbolTo(req.getSymbolTo());
        readReq.setSymbolFrom(req.getSymbolFrom());
        readReq.setInterval(req.getInterval());
        readReq.setTimeFrom(startDate);
        readReq.setTimeTo(endDate);

        return influxApiClient
                .post()
                .uri("/alphavantage/forex/read/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(readReq))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ForexTimeseriesDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

    public List<Forex> getAllForex(){
        return forexRepository.findAll();
    }

}
