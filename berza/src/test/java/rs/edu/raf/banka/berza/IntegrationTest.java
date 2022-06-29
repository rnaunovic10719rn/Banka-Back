package rs.edu.raf.banka.berza;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"berza.berze.csv=./berze.csv", "berza.inflacije.csv=./inflacije.csv"})
@AutoConfigureMockMvc
@DisabledIfEnvironmentVariable(named = "WORKSPACE", matches = "CI")
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String token;


    @BeforeEach
    public void setUp() throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "admin");
        jsonObject.put("password", "Admin123");
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:8080/api/login", HttpMethod.POST, entity, String.class);
        token = response.getBody().replace("\"", "");
    }

    @Test
    void getOdabraneAkcije() throws Exception {
        mockMvc.perform(get("/api/akcije/podaci")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void getAkcijeById() throws Exception {
        mockMvc.perform(get("/api/akcije/podaci/{ticker}","1")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void getAkcijeById2() throws Exception {
        mockMvc.perform(get("/api/akcije/podaci/id/{id}",1L)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void getAkcijeTimeseries() throws Exception {
        mockMvc.perform(get("/api/akcije/podaci/timeseries/{type}/{symbol}","1","1")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isBadRequest());

    }


}
