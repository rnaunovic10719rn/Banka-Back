package rs.edu.raf.banka.racun;

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
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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
    void getTransakcije() throws Exception {
        mockMvc.perform(get("/api/racun/transakcije")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    void getTransakcijeInvalidToken() throws Exception {
        mockMvc.perform(get("/api/racun/transakcije")
                .header("Authorization", "InvalidToken")
                .contentType("application/json")
                .content(""))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStanjeSupervisor() throws Exception {
        mockMvc.perform(get("/api/racun/stanjeSupervisor")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    void getStanjeSupervisorInvalidToken() throws Exception {
        mockMvc.perform(get("/api/racun/stanjeSupervisor")
                .contentType("application/json")
                .header("Authorization", "InvalidToken")
                .content(""))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStanjeAgent() throws Exception {
        mockMvc.perform(get("/api/racun/stanjeAgent")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    void getStanjeAgentInvalidToken() throws Exception {
        mockMvc.perform(get("/api/racun/stanjeAgent")
                .contentType("application/json")
                .header("Authorization", "InvalidToken")
                .content(""))
                .andExpect(status().isForbidden());
    }


}
