package rs.edu.raf.banka.berza;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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
import rs.edu.raf.banka.berza.controller.BerzaController;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.model.Valuta;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.requests.AkcijaCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.service.impl.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        Assert.assertEquals(akcijePodaciService.getOdabraneAkcije().get(0).getBerzaId(), akcijePodaciDtos.get(0).getBerzaId());
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
        ResultActions resultActions =  mockMvc.perform(get("/api/berza")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<Berza> berzas = objectMapper.readValue(strResp, ArrayList.class);
        assertThat(berzas).isNotNull();

        Assert.assertEquals(berzaService.findAll().size(), berzas.size());

    }

    @Test
    void findBerzaById() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/berza/id/{id}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        Berza berzas = objectMapper.readValue(strResp, Berza.class);
        assertThat(berzas).isNotNull();

        Assert.assertEquals(berzaService.findBerza(1L).getOznakaBerze(), berzas.getOznakaBerze());
    }

    @Test
    void findAkcija() throws Exception {

        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setBerzaOznaka("BBJ");
        akcijaCreateUpdateRequest.setOznaka("BBJ");
        akcijaCreateUpdateRequest.setOutstandingShares(1L);
        akcijaCreateUpdateRequest.setOpis("mock");
        berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);

        ResultActions resultActions = mockMvc.perform(get("/api/berza/{oznaka}", "BBJ")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        Akcije akcija = objectMapper.readValue(strResp, Akcije.class);
        assertThat(akcija).isNotNull();

        Assert.assertEquals(berzaService.findAkcije("BBJ").getBerza().getId(), akcija.getBerza().getId());

    }

    @Test
    void getOrders() throws Exception {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.AKCIJA;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.SELL;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 0.0;

        Order order = new Order();
        order.setUserId(userAccount);
        order.setHartijaOdVrednostiId(hartijaOdVrednostiId);
        order.setHartijaOdVrednosti(hartijaOdVrednostiType);
        order.setKolicina(kolicina);
        order.setOrderAction(orderAction);
        order.setPredvidjenaCena(ukupnaCena);
        order.setProvizija(provizija);
        order.setOrderType(orderType);
        order.setAON(isAON);
        order.setMargin(isMargin);
        order.setHartijaOdVrednostiSymbol(oznakaHartije);
        order.setAsk(ask);
        order.setBid(bid);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setOrderStatus(OrderStatus.APPROVED);
        order.setBerza(berza);

        var request = new OrderRequest();
        request.setSymbol(oznakaHartije);
        request.setHartijaOdVrednostiTip(hartijaOdVrednostiType.toString());
        request.setAkcija("buy");
        request.setKolicina(kolicina);
        request.setAllOrNoneFlag(isAON);
        request.setMarginFlag(isMargin);

        orderRepository.save(order);


        ResultActions resultActions = mockMvc.perform(get("/api/berza/order/{status}/{done}", "APPROVED", false)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("")).andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Order.class);
        List<Order> orders = objectMapper.readValue(strResp, listType);

        Assert.assertEquals(orderService.getOrders("Bearer " + token, "APPROVED", false).get(0).getBid(),orders.get(0).getBid());

    }

    @Test
    void makeOrder() throws Exception {
        AkcijePodaciDto akcije = new AkcijePodaciDto();
        akcije.setId(1L);
        akcije.setBerzaId(2L);
        akcije.setPrice(10.0);

        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);

        Valuta valuta = new Valuta();
        valuta.setKodValute("EUR");

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");
        berza.setValuta(valuta);

        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.AKCIJA.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        request.setLimitValue(5);
        request.setStopValue(10);
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);

        AskBidPriceDto askBidPrice = new AskBidPriceDto();
        askBidPrice.setHartijaId(1L);
        askBidPrice.setAsk(10.0);
        askBidPrice.setBid(10.0);
        askBidPrice.setBerza(berza);

        Akcije a = new Akcije();
        a.setBerza(berza);
        a.setOznakaHartije("usd");
        a.setLastUpdated(new Date());
        akcijeRepository.save(a);


        mockMvc.perform(post("/api/berza/order", true)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(asJsonString(request))).andExpect(status().isOk());

        Assert.assertEquals(orderService.getOrders("Bearer " + token).get(0),order);

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
