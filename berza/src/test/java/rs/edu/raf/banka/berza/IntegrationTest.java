package rs.edu.raf.banka.berza;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.service.impl.AkcijePodaciService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"berza.berze.csv=./berze.csv", "berza.inflacije.csv=./inflacije.csv"})
@AutoConfigureMockMvc
@DisabledIfEnvironmentVariable(named = "WORKSPACE", matches = "CI")
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    AkcijePodaciService akcijePodaciService;


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

        ResultActions resultActions = mockMvc.perform(get("/api/akcije/podaci")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<AkcijePodaciDto> akcijePodaciDtos = objectMapper.readValue(strResp, ArrayList.class);
        assertThat(akcijePodaciDtos).isNotNull();

        Assert.assertEquals(akcijePodaciService.getOdabraneAkcije().size(), akcijePodaciDtos.size());
    }

    @Test
    void getAkcijeById() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/akcije/podaci/{ticker}", "AAPL")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        AkcijePodaciDto akcijePodaciDto = objectMapper.readValue(strResp, AkcijePodaciDto.class);
        assertThat(akcijePodaciDto).isNotNull();

        Assert.assertEquals(akcijePodaciService.getAkcijaByTicker("AAPL").getId(), akcijePodaciDto.getId());
    }

    @Test
    void getAkcijeById2() throws Exception {

        ResultActions resultActions =   mockMvc.perform(get("/api/akcije/podaci/id/{id}", akcijePodaciService.getAkcijaByTicker("AAPL").getId())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        AkcijePodaciDto akcijePodaciDto = objectMapper.readValue(strResp, AkcijePodaciDto.class);
        assertThat(akcijePodaciDto).isNotNull();

        Assert.assertEquals(akcijePodaciService.getAkcijaByTicker("AAPL").getId(), akcijePodaciDto.getId());
    }

    @Test
    void getAkcijeTimeseries() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/akcije/podaci/timeseries/{type}/{symbol}", "1m", "AAPL")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<AkcijeTimeseriesDto> akcijePodaciDto = objectMapper.readValue(strResp, ArrayList.class);
        assertThat(akcijePodaciDto).isNotNull();

        Assert.assertEquals(akcijePodaciService.getAkcijeTimeseries(AkcijeTimeseriesUpdateRequest.getForType("1m", "AAPL")).size(), akcijePodaciDto.size());
    }

    @Test
    void findAll() throws Exception {
        mockMvc.perform(get("/api/berza")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void findBerzaById() throws Exception {
        mockMvc.perform(get("/api/berza/id/{id}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void findAkcija() throws Exception {
        mockMvc.perform(get("/api/berza/{oznaka}", "1")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void getOrders() throws Exception {
        mockMvc.perform(get("/api/berza/order/{status}/{done}", "1", true)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }

    @Test
    void makeOrder() throws Exception {
        mockMvc.perform(get("/api/berza/order", "1", true)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

    }
}
