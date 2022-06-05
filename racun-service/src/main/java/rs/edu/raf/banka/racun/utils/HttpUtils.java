package rs.edu.raf.banka.racun.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.racun.requests.ChangeUserLimitRequest;

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

}
