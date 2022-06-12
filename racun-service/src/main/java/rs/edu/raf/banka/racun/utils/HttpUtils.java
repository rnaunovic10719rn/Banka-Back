package rs.edu.raf.banka.racun.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.racun.dto.AkcijePodaciDto;
import rs.edu.raf.banka.racun.dto.ForexPodaciDto;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.requests.ChangeUserLimitRequest;

import java.net.URI;

public class HttpUtils {

    public static ResponseEntity<String> updateUserLimit(String url, String token, double limitDelta) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ChangeUserLimitRequest newLimit = new ChangeUserLimitRequest();
        newLimit.setLimitDelta(limitDelta);

        HttpEntity<ChangeUserLimitRequest> entity = new HttpEntity<>(newLimit, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public static ResponseEntity<ForexPodaciDto> getExchangeRate(String url, String token, String from, String to) {
        URI main = URI.create(url);
        URI exchangeRateUrl = main.resolve("./" + from + "/" + to);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(exchangeRateUrl.toString(), HttpMethod.GET, entity, ForexPodaciDto.class);
    }

    public static ResponseEntity<AkcijePodaciDto> getAkcijeById(String url, Long id) {
        URI main = URI.create(url);
        URI akcijeByIdUrl = main.resolve("./" + id);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(akcijeByIdUrl.toString(), HttpMethod.GET, entity, AkcijePodaciDto.class);
    }

    public static ResponseEntity<UserDto> getUser(String url, String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);

        return response;
    }

}
