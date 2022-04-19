package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesReadRequest;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

@Service
public class FuturesUgovoriPodaciService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final WebClient influxApiClient;

    private FuturesUgovoriRepository futuresUgovoriRepository;

    private List<String> odabraniFuturesUgovori = Arrays.asList("CONFH2022", "FBTPH2022", "FBTPM2022", "FBTSH2022", "FBUTQ2022");

    @Autowired
    public FuturesUgovoriPodaciService(FuturesUgovoriRepository futuresUgovoriRepository, WebClient influxApiClient){
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.influxApiClient = influxApiClient;
    }

    public List<FuturesPodaciDto> getOdabraniFuturesUgovori() {
        List<FuturesPodaciDto> podaci = new ArrayList<>();

        for(String f: odabraniFuturesUgovori) {
           podaci.add(this.getFuturesUgovor(f));
        }

        return podaci;
    }

    public FuturesPodaciDto getFuturesUgovor(String symbol) {
        FuturesUgovori future = futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(symbol);
        if(future == null){
            future = new FuturesUgovori();
            future.setOznakaHartije(symbol);
            futuresUgovoriRepository.save(future);
        }

        final HashMap<String, String> params = new HashMap<>();
        params.put("symbol", symbol);
        params.put("timeFrom", "2022-04-05T13:34:51.966Z");
        params.put("timeTo", "2022-04-10T13:34:51.966Z");

        List<FuturesPodaciDto> res = influxApiClient
                .post()
                .uri("/nasdaq/future/updateread")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FuturesPodaciDto>>() {})
                .block(REQUEST_TIMEOUT);

        if(res != null && res.size() > 0) {
            FuturesPodaciDto futuresPodaciDto = res.get(res.size()-1);
            futuresPodaciDto.setId(future.getId());
            return futuresPodaciDto;
        }

        return null;
    }

    public List<FuturesTimeseriesDto> getFuturesTimeseries(String type, String symbol) {
        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String endDate = zonedDateTime.format(endFormatter);

        if(type.equals("1d")) {
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
        } else if(type.equals("5d")) {
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
            switch (type) {
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

        AkcijeTimeseriesReadRequest readReq = new AkcijeTimeseriesReadRequest();
        readReq.setSymbol(symbol);
        readReq.setTimeFrom(startDate);
        readReq.setTimeTo(endDate);

        return influxApiClient
                .post()
                .uri("/nasdaq/future/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(readReq))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FuturesTimeseriesDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

}
