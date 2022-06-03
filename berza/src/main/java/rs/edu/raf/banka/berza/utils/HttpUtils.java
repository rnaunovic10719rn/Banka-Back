package rs.edu.raf.banka.berza.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.berza.dto.UserDto;

public class HttpUtils {

    public static ResponseEntity<UserDto> getUser(String url, String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);

        return response;
    }

}
