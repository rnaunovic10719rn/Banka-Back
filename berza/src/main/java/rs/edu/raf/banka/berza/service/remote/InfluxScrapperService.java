package rs.edu.raf.banka.berza.service.remote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.*;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesReadRequest;
import rs.edu.raf.banka.berza.dto.request.ForexExchangeRequest;
import rs.edu.raf.banka.berza.dto.request.ForexTimeseriesReadRequest;
import rs.edu.raf.banka.berza.dto.request.FuturesTimeseriesReadRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class InfluxScrapperService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final WebClient influxApiClient;

    @Autowired
    public InfluxScrapperService(WebClient influxApiClient){
        this.influxApiClient = influxApiClient;
    }

    public List<AkcijePodaciDto> getStocksQuote(List<String> symbols) {
        HashMap<String, List<String>> req = new HashMap<>();
        req.put("symbols", symbols);

        return influxApiClient
                .post()
                .uri("/alphavantage/stock/quote/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AkcijePodaciDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

    public List<AkcijeTimeseriesDto> getStocksTimeseries(AkcijeTimeseriesReadRequest req) {
        return influxApiClient
                .post()
                .uri("/alphavantage/stock/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AkcijeTimeseriesDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

    public List<ForexPodaciDto> getForexQuote(String from, String to) {
        List<ForexExchangeRequest> reqs = new ArrayList<>();
        ForexExchangeRequest fer = new ForexExchangeRequest();
        fer.setSymbolFrom(from);
        fer.setSymbolTo(to);
        reqs.add(fer);

        HashMap<String, List<ForexExchangeRequest>> req = new HashMap<>();
        req.put("currencies", reqs);

        return influxApiClient
                .post()
                .uri("alphavantage/forex/exchangerate/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ForexPodaciDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

    public List<ForexTimeseriesDto> getForexTimeseries(ForexTimeseriesReadRequest req) {
        return influxApiClient
                .post()
                .uri("/alphavantage/forex/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ForexTimeseriesDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

    public List<FuturesPodaciDto> getFuturesQoute(String symbol) {
        final HashMap<String, String> params = new HashMap<>();
        // TODO: ispraviti ovo
        params.put("symbol", symbol);
        params.put("timeFrom", "2022-04-05T13:34:51.966Z");
        params.put("timeTo", "2022-04-10T13:34:51.966Z");

        return influxApiClient
                .post()
                .uri("/nasdaq/future/updateread")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FuturesPodaciDto>>() {})
                .block(REQUEST_TIMEOUT);
    }

    public List<FuturesTimeseriesDto> getFuturesTimeseries(FuturesTimeseriesReadRequest req) {
        return influxApiClient
                .post()
                .uri("/nasdaq/future/updateread/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(req))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FuturesTimeseriesDto>>() {})
                .block(REQUEST_TIMEOUT);
    }
}
