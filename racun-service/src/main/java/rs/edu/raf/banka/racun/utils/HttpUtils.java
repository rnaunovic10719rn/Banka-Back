package rs.edu.raf.banka.racun.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.racun.dto.*;
import rs.edu.raf.banka.racun.requests.ChangeUserLimitRequest;
import rs.edu.raf.banka.racun.response.AskBidPriceResponse;

import java.net.URI;

public class HttpUtils {

    private static final String USER_SERVICE_ENDPOINT_LIMIT_CHANGE = "api/limit-change";
    private static final String USER_SERVICE_ENDPOINT_GET_USER = "api/user";
    private static final String BERZA_SERVICE_ENDPOINT_STOCKS_ID = "api/akcije/podaci/id/";
    private static final String BERZA_SERVICE_ENDPOINT_FUTURES_ID = "api/futures/podaci/id/";
    private static final String BERZA_SERVICE_ENDPOINT_EXCHANGE_RATE = "api/forex/podaci/";
    private static final String BERZA_SERVICE_ENDPOINT_GET_BERZA = "api/berza/id/";
    private static final String BERZA_SERVICE_ENDPOINT_ASK_BID = "api/berza/askbid/";

    public static ResponseEntity<String> updateUserLimit(String userServiceBaseURL, String token, double limitDelta) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ChangeUserLimitRequest newLimit = new ChangeUserLimitRequest();
        newLimit.setLimitDelta(limitDelta);

        HttpEntity<ChangeUserLimitRequest> entity = new HttpEntity<>(newLimit, headers);
        String url = userServiceBaseURL + USER_SERVICE_ENDPOINT_LIMIT_CHANGE;
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public static ResponseEntity<UserDto> getUser(String userServiceBaseURL, String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String url = userServiceBaseURL + USER_SERVICE_ENDPOINT_GET_USER;
        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);

        return response;
    }

    public static ResponseEntity<ForexPodaciDto> getExchangeRate(String berzaServiceBaseUrl, String token, String from, String to) {
        URI main = URI.create(berzaServiceBaseUrl + BERZA_SERVICE_ENDPOINT_EXCHANGE_RATE);
        URI exchangeRateUrl = main.resolve("./" + from + "/" + to);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(exchangeRateUrl.toString(), HttpMethod.GET, entity, ForexPodaciDto.class);
    }

    public static ResponseEntity<AkcijePodaciDto> getAkcijeById(String berzaServiceBaseUrl, Long id) {
        URI main = URI.create(berzaServiceBaseUrl + BERZA_SERVICE_ENDPOINT_STOCKS_ID);
        URI akcijeByIdUrl = main.resolve("./" + id);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(akcijeByIdUrl.toString(), HttpMethod.GET, entity, AkcijePodaciDto.class);
    }

    public static ResponseEntity<FuturesPodaciDto> getFuturesById(String berzaServiceBaseUrl, Long id) {
        URI main = URI.create(berzaServiceBaseUrl + BERZA_SERVICE_ENDPOINT_FUTURES_ID);
        URI futuresByIdUrl = main.resolve("./" + id);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(futuresByIdUrl.toString(), HttpMethod.GET, entity, FuturesPodaciDto.class);
    }

    public static ResponseEntity<BerzaDto> getBerzaById(String berzaServiceBaseUrl, Long id) {
        URI main = URI.create(berzaServiceBaseUrl + BERZA_SERVICE_ENDPOINT_GET_BERZA);
        URI berzaByIdUrl = main.resolve("./" + id);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(berzaByIdUrl.toString(), HttpMethod.GET, entity, BerzaDto.class);
    }

    public static ResponseEntity<AskBidPriceResponse> getAskBidPrice(String berzaServiceBaseUrl, String hartijaType, String symbol) {
        URI main = URI.create(berzaServiceBaseUrl + BERZA_SERVICE_ENDPOINT_ASK_BID);
        URI askBidUrl = main.resolve("./" + hartijaType.toUpperCase() + "/" + symbol);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(askBidUrl.toString(), HttpMethod.GET, entity, AskBidPriceResponse.class);
    }

}
