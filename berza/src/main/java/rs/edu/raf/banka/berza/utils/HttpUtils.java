package rs.edu.raf.banka.berza.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.requests.TransakcijaRequest;
import rs.edu.raf.banka.berza.response.TransakcijaResponse;

import java.net.URI;

public class HttpUtils {

    public static ResponseEntity<UserDto> getUser(String url, String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);

        return response;
    }

    public static ResponseEntity<TransakcijaResponse> postTransaction(String url, String token, TransakcijaRequest transakcija) {
        URI main = URI.create(url);
        URI transactionUrl = main.resolve("./racun/transakcija");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<TransakcijaRequest> entity = new HttpEntity<>(transakcija, headers);
        return restTemplate.exchange(transactionUrl.toString(), HttpMethod.POST, entity, TransakcijaResponse.class);
    }

    public static RetryTemplate retryTemplate() {

        int maxAttempt = 10;
        Long retryTimeInterval = 2L;

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempt);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryTimeInterval);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

}
