package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;

import java.time.Duration;
import java.util.*;

@Service
public class FuturesUgovoriPodaciService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

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
        final HashMap<String, String> params = new HashMap<>();
        params.put("symbol", symbol);

        influxApiClient
                .post()
                .uri("/nasdaq/futures/updatewait")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FuturesPodaciDto>>() {})
                .block(REQUEST_TIMEOUT);

        params.put("timeFrom", "2022-04-05T13:34:51.966Z");
        params.put("timeTo", "2022-04-10T13:34:51.966Z");

        List<FuturesPodaciDto> res = influxApiClient
                .post()
                .uri("/nasdaq/futures/read")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FuturesPodaciDto>>() {})
                .block(REQUEST_TIMEOUT);

        if(res != null && res.size() > 0) {
            return res.get(res.size()-1);
        }

        return null;
    }

}
