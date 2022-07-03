package rs.edu.raf.banka.berza;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.dto.request.ForexTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.*;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.requests.AkcijaCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.FuturesCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.service.impl.*;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    ForexPodaciService forexPodaciService;

    @Autowired
    BerzaService berzaService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PriceService priceService;

    @Autowired
    AkcijeRepository akcijeRepository;


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

        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, AkcijePodaciDto.class);
        List<AkcijePodaciDto> akcijePodaciDtos = objectMapper.readValue(strResp, listType);
        assertThat(akcijePodaciDtos).isNotNull();
        assertThat(akcijePodaciDtos.size()).isEqualTo(5);

        for(AkcijePodaciDto akcijePodaciDto: akcijePodaciDtos) {
            assertThat(akcijePodaciDto).isNotNull();
        }
    }

    @Test
    void getOdabraniForexParovi() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/forex/podaci")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, ForexPodaciDto.class);
        List<ForexPodaciDto> forexPodaciDtos = objectMapper.readValue(strResp, listType);
        assertThat(forexPodaciDtos).isNotNull();
        assertThat(forexPodaciDtos.size()).isEqualTo(3);

        for(ForexPodaciDto forexPodaciDto: forexPodaciDtos) {
            assertThat(forexPodaciDto).isNotNull();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"1d", "5d", "1m", "6m", "1y", "ytd", "2y"})
    void getAkcijeTimeseries(String type) throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/akcije/podaci/timeseries/{type}/{symbol}", type, "AAPL")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<AkcijePodaciDto> akcijePodaciDto = objectMapper.readValue(strResp, ArrayList.class);
        assertThat(akcijePodaciDto).isNotNull();

        Assertions.assertEquals(akcijePodaciService.getAkcijeTimeseries(AkcijeTimeseriesUpdateRequest.getForType(type, "AAPL")).size(), akcijePodaciDto.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"5d", "1m", "6m", "1y", "ytd", "2y"})
    void getForexTimeseries(String type) throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/forex/podaci/timeseries/{type}/{from}/{to}", type, "EUR", "USD")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<ForexPodaciDto> forexPodaciDtos = objectMapper.readValue(strResp, ArrayList.class);
        assertThat(forexPodaciDtos).isNotNull();
        assertThat(forexPodaciDtos.size()).isNotZero();
    }

    /**
     * BERZA CONTROLLER
     */

    @Test
    void findAll() throws Exception {
        ResultActions resultActions =  mockMvc.perform(get("/api/berza")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<Berza> berze = objectMapper.readValue(strResp, ArrayList.class);
        assertThat(berze).isNotNull();

        Assertions.assertEquals(berzaService.findAll().size(), berze.size());

    }

    @Test
    void findBerzaById() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/berza/id/{id}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        Berza berze = objectMapper.readValue(strResp, Berza.class);
        assertThat(berze).isNotNull();

        Assertions.assertEquals(berzaService.findBerza(1L).getOznakaBerze(), berze.getOznakaBerze());
    }

    @Test
    void createUpdateAndFindStock() throws Exception {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setBerzaOznaka("BBJ");
        akcijaCreateUpdateRequest.setOznaka("BBJ");
        akcijaCreateUpdateRequest.setOutstandingShares(1L);
        akcijaCreateUpdateRequest.setOpis("mock");

        mockMvc.perform(post("/api/berza/hartija/akcija")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(asJsonString(akcijaCreateUpdateRequest))).andExpect(status().isOk());

        ResultActions resultActions = mockMvc.perform(get("/api/berza/{oznaka}", "BBJ")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        Akcije akcija = objectMapper.readValue(strResp, Akcije.class);
        assertThat(akcija).isNotNull();

        Akcije akcijaSvc = berzaService.findAkcije("BBJ");
        assertThat(akcijaSvc).isNotNull();
        assertThat(akcijaSvc.getCustom()).isTrue();

        Assertions.assertEquals(akcijaSvc.getBerza().getId(), akcija.getBerza().getId());

        akcijaCreateUpdateRequest.setId(akcijaSvc.getId());
        akcijaCreateUpdateRequest.setOznaka("META");
        mockMvc.perform(put("/api/berza/hartija/akcija")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(asJsonString(akcijaCreateUpdateRequest))).andExpect(status().isOk());

        assertThat(berzaService.findAkcije("META")).isNotNull();
    }

    @Test
    void createUpdateAndFindFutures() throws Exception {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setOznaka("CONFH2022");
        futuresCreateUpdateRequest.setOpis("Futura");
        futuresCreateUpdateRequest.setBerzaOznaka("BBJ");
        futuresCreateUpdateRequest.setContractSize(10.0);
        futuresCreateUpdateRequest.setContractUnit("barrel");
        futuresCreateUpdateRequest.setMaintenanceMargin(10.0);
        futuresCreateUpdateRequest.setSettlementDate(new Date());

        mockMvc.perform(post("/api/berza/hartija/future")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(asJsonString(futuresCreateUpdateRequest))).andExpect(status().isOk());

        ResultActions resultActions = mockMvc.perform(get("/api/berza/hartija/future")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, FuturesUgovori.class);
        List<FuturesUgovori> futuresUgovori = objectMapper.readValue(strResp, listType);
        assertThat(futuresUgovori).isNotNull();
        assertThat(futuresUgovori.size()).isNotZero();

        futuresCreateUpdateRequest.setId(futuresUgovori.get(0).getId());
        futuresCreateUpdateRequest.setOznaka("CONFF2022");
        mockMvc.perform(put("/api/berza/hartija/future")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(asJsonString(futuresCreateUpdateRequest))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void makeOrder(boolean margin) throws Exception {
       OrderRequest orderRequest = new OrderRequest();
       orderRequest.setSymbol("AAPL");
       orderRequest.setHartijaOdVrednostiTip("AKCIJA");
       orderRequest.setKolicina(1);
       orderRequest.setAkcija("BUY");
       orderRequest.setLimitValue(0);
       orderRequest.setStopValue(0);
       orderRequest.setAllOrNoneFlag(false);
       orderRequest.setMarginFlag(margin);

       mockMvc.perform(post("/api/berza/order", true)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(asJsonString(orderRequest))).andExpect(status().isOk());


       boolean val = false;
       for(int i = 0; i < 10; i++) {
           ResultActions resultActions = mockMvc.perform(get("/api/berza/order")
                   .header("Authorization", "Bearer " + token)
                   .contentType("application/json")
                   .content("")).andExpect(status().isOk());

           MvcResult mvcResult = resultActions.andReturn();
           String strResp = mvcResult.getResponse().getContentAsString();

           List<Order> orders = objectMapper.readValue(strResp, new TypeReference<List<Order>>() {
           });
           assertThat(orders.size()).isNotZero();

           for(Order order: orders) {
               if(order.isMargin() != margin) {
                   continue;
               }
               if (!order.getDone()) {
                   Thread.sleep(10000);
               }
           }

           val = true;
       }
       assertThat(val).isTrue();
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
