package rs.edu.raf.banka.berza;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.*;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.requests.AkcijaCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.FuturesCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.service.impl.*;
import rs.edu.raf.banka.berza.service.remote.TransakcijaService;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BerzaServiceTest {


    @InjectMocks
    BerzaService berzaService;

    @Mock
    BerzaRepository berzaRepository;

    @Mock
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    ForexPodaciService forexPodaciService;

    @Mock
    AkcijeRepository akcijeRepository;

    @Mock
    FuturesUgovoriRepository futuresUgovoriRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserService userService;

    @Mock
    OrderService orderService;

    @Mock
    PriceService priceService;

    @Mock
    TransakcijaService transakcijaService;

    @Mock
    EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        orderService = Mockito.spy(new OrderService(orderRepository, futuresUgovoriPodaciService, priceService, userService, transakcijaService, entityManager));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindAll() {
        Berza berza1 = new Berza();
        Berza berza2 = new Berza();
        when(berzaRepository.findAll()).thenReturn(List.of(new Berza[]{berza1, berza2}));
        assertEquals(2, berzaService.findAll().size());
    }

    @Test
    void testFindBerza() {
        String oznaka = "Oznaka";
        Berza berza = new Berza();
        berza.setNaziv("Berza");
        when(berzaRepository.findBerzaByOznakaBerze(oznaka)).thenReturn(berza);
        assertEquals("Berza", berzaService.findBerza(oznaka).getNaziv());
    }

    @Test
    void testFindBerzaLong() {
        Long oznaka = 1L;
        Berza berza = new Berza();
        berza.setNaziv("Berza");
        berza.setOznakaBerze("oznaka");
        Valuta valuta = new Valuta();
        valuta.setKodValute("kod");
        berza.setValuta(valuta);
        when(berzaRepository.findBerzaById(any())).thenReturn(berza);
        assertEquals("oznaka", berzaService.findBerza(oznaka).getOznakaBerze());
    }

    @Test
    void testFindAkcije() {
        String symbol = "Simbol";
        Akcije akcija = new Akcije();
        akcija.setOpisHartije("MojOpis");
        when(akcijeRepository.findAkcijeByOznakaHartije(symbol)).thenReturn(akcija);
        assertEquals("MojOpis", berzaService.findAkcije(symbol).getOpisHartije());
    }

    @Test
    void testGetCommission(){
        OrderType orderType = OrderType.MARKET_ORDER;
        double price = 100.00;
        assertEquals(Math.min(0.14 * price, 7), berzaService.getCommission(price,orderType));
    }

    @Test
    void testGetCommission2(){
        OrderType orderType = OrderType.LIMIT_ORDER;
        double price = 100.00;
        assertEquals(Math.min(0.24 * price, 12), berzaService.getCommission(price,orderType));
    }

    @Test
    void testCanExecuteTransactionSellSTOP_LIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setAsk(ask);
        order.setOrderType(OrderType.STOP_LIMIT_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertFalse(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionSellMARKET_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setAsk(ask);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertFalse(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionButMARKET_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setBid(bid);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertFalse(orderService.canExecuteTransactionBuy(order));
    }

    @Test
    void testCanExecuteTransactionButSTOP_LIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setBid(bid);
        order.setOrderType(OrderType.STOP_LIMIT_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertFalse(orderService.canExecuteTransactionBuy(order));
    }

    @Test
    void testGetPrice(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.SELL);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 12.00;
        Double bid = 1.00;

        assertEquals(ask, berzaService.getPrice(ask,bid,OrderAction.SELL));
    }

    @Test
    void testGetPriceBUY(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 12.00;
        Double bid = 1.00;

        assertEquals(bid, berzaService.getPrice(ask,bid,OrderAction.BUY));
    }



    @Test
    void testGetOrderTypeSTOPLIMIT(){
        Integer limitValue = 1;
        Integer stopValue = 1;
        assertEquals(OrderType.STOP_LIMIT_ORDER, berzaService.getOrderType(limitValue, stopValue));
    }
    @Test
    void testGetOrderTypeLIMIT(){
        Integer limitValue = 1;
        Integer stopValue = 0;
        assertEquals(OrderType.LIMIT_ORDER, berzaService.getOrderType(limitValue, stopValue));
    }
    @Test
    void testGetOrderTypeSTOP(){
        Integer limitValue = 0;
        Integer stopValue = 1;
        assertEquals(OrderType.STOP_ORDER, berzaService.getOrderType(limitValue, stopValue));
    }
    @Test
    void testGetOrderTypeMARKET(){
        Integer limitValue = 0;
        Integer stopValue = 0;
        assertEquals(OrderType.MARKET_ORDER, berzaService.getOrderType(limitValue, stopValue));
    }

    @Test
    void testGetOrderStatusRSD(){
        String token = "token";
        double price = 10.0;
        String valuta = "RSD";
        UserDto user = new UserDto();
        user.setLimit(100.0);
        user.setLimitUsed(11.0);
        user.setNeedsSupervisorPermission(false);

        when(userService.getUserByToken(token)).thenReturn(user);
        when(userService.getUserRoleByToken(token)).thenReturn(String.valueOf(UserRole.ROLE_AGENT));
        assertEquals(OrderStatus.APPROVED, berzaService.getOrderStatus(token, price, valuta, false));
    }

    @Test
    void testGetOrderStatusRSDHOLD(){
        String token = "token";
        double price = 10.0;
        String valuta = "RSD";
        UserDto user = new UserDto();
        user.setLimit(100.0);
        user.setLimitUsed(11.0);
        user.setNeedsSupervisorPermission(true);

        when(userService.getUserByToken(token)).thenReturn(user);
        when(userService.getUserRoleByToken(token)).thenReturn(String.valueOf(UserRole.ROLE_AGENT));
        assertEquals(OrderStatus.ON_HOLD, berzaService.getOrderStatus(token, price, valuta, false));
    }

    @Test
    void testGetOrderStatusEUR(){
        String token = "token";
        double price = 10.0;
        String valuta = "EUR";
        UserDto user = new UserDto();
        user.setLimit(100.0);
        user.setLimitUsed(11.0);
        user.setNeedsSupervisorPermission(false);
        ForexPodaciDto exchangeRate = new ForexPodaciDto();
        exchangeRate.setExchangeRate(1.0);

        when(userService.getUserByToken(token)).thenReturn(user);
        when(userService.getUserRoleByToken(token)).thenReturn(String.valueOf(UserRole.ROLE_AGENT));
        when(forexPodaciService.getForexBySymbol(valuta, "RSD")).thenReturn(exchangeRate);
        assertEquals(OrderStatus.APPROVED, berzaService.getOrderStatus(token, price, valuta, false));
    }

    @Test
    void testMakeOrderAkcijaStopOrder(){
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

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";


        AskBidPriceDto askBidPrice = new AskBidPriceDto();
        askBidPrice.setHartijaId(1L);
        askBidPrice.setAsk(10.0);
        askBidPrice.setBid(10.0);
        askBidPrice.setBerza(berza);

        UserDto user = new UserDto();
        user.setLimit(100.0);
        user.setLimitUsed(11.0);
        user.setNeedsSupervisorPermission(false);
        user.setId(1L);


        when(priceService.getAskBidPrice(any(), (String) any())).thenReturn(askBidPrice);
        when(userService.getUserByToken(any())).thenReturn(user);
        when(userService.getUserRoleByToken(any())).thenReturn(String.valueOf(UserRole.ROLE_ADMIN));
        assertEquals(new OrderResponse(MessageUtils.ERROR).getMessage(),berzaService.makeOrder(token, request).getMessage());
    }

    @Test
    void testCreateUpdateAkcija1() {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setOznaka("META");
        akcijaCreateUpdateRequest.setOpis("Meta Platforms Inc");
        akcijaCreateUpdateRequest.setBerzaOznaka("NASDAQ");
        akcijaCreateUpdateRequest.setOutstandingShares(10L);

        when(berzaRepository.findBerzaByOznakaBerze("NASDAQ")).thenReturn(new Berza());

        berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);
    }

    @Test
    void testCreateUpdateAkcija2() {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setId(1L);
        akcijaCreateUpdateRequest.setOznaka("META");
        akcijaCreateUpdateRequest.setOpis("Meta Platforms Inc");
        akcijaCreateUpdateRequest.setBerzaOznaka("NASDAQ");
        akcijaCreateUpdateRequest.setOutstandingShares(10L);

        Akcije akcija = new Akcije();
        akcija.setCustom(true);

        when(berzaRepository.findBerzaByOznakaBerze("NASDAQ")).thenReturn(new Berza());
        when(akcijeRepository.findAkcijeById(1L)).thenReturn(akcija);

        berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);
    }

    @Test
    void testCreateUpdateAkcija3() {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setId(1L);
        akcijaCreateUpdateRequest.setOznaka("META");
        akcijaCreateUpdateRequest.setOpis("Meta Platforms Inc");
        akcijaCreateUpdateRequest.setBerzaOznaka("NASDAQ");
        akcijaCreateUpdateRequest.setOutstandingShares(10L);

        Akcije akcija = new Akcije();
        akcija.setCustom(false);

        when(berzaRepository.findBerzaByOznakaBerze("NASDAQ")).thenReturn(new Berza());
        when(akcijeRepository.findAkcijeById(1L)).thenReturn(akcija);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);
        });

        String expectedMessage = "stock is not custom";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateAkcija4() {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setId(1L);
        akcijaCreateUpdateRequest.setOznaka("META");
        akcijaCreateUpdateRequest.setOpis("Meta Platforms Inc");
        akcijaCreateUpdateRequest.setBerzaOznaka("NASDAQ");
        akcijaCreateUpdateRequest.setOutstandingShares(10L);

        Akcije akcija = new Akcije();
        akcija.setCustom(false);

        when(berzaRepository.findBerzaByOznakaBerze("NASDAQ")).thenReturn(new Berza());
        when(akcijeRepository.findAkcijeById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);
        });

        String expectedMessage = "stock not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateAkcija5() {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setId(1L);
        akcijaCreateUpdateRequest.setOznaka("META");
        akcijaCreateUpdateRequest.setOpis("Meta Platforms Inc");
        akcijaCreateUpdateRequest.setBerzaOznaka("NASDAQ");
        akcijaCreateUpdateRequest.setOutstandingShares(10L);

        Akcije akcija = new Akcije();
        akcija.setCustom(false);

        when(berzaRepository.findBerzaByOznakaBerze("NASDAQ")).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);
        });

        String expectedMessage = "exchange not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateAkcija6() {
        AkcijaCreateUpdateRequest akcijaCreateUpdateRequest = new AkcijaCreateUpdateRequest();
        akcijaCreateUpdateRequest.setId(1L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateAkcija(akcijaCreateUpdateRequest);
        });

        String expectedMessage = "bad request";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateFuturesUgovor1() {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setOznaka("CONFH2022");
        futuresCreateUpdateRequest.setOpis("Some random future");
        futuresCreateUpdateRequest.setBerzaOznaka("EUREX");
        futuresCreateUpdateRequest.setContractSize(0.0);
        futuresCreateUpdateRequest.setMaintenanceMargin(0.0);
        futuresCreateUpdateRequest.setContractUnit("unit");
        futuresCreateUpdateRequest.setSettlementDate(new Date());

        when(berzaRepository.findBerzaByOznakaBerze("EUREX")).thenReturn(new Berza());

        berzaService.createUpdateFuturesUgovor(futuresCreateUpdateRequest);
    }

    @Test
    void testCreateUpdateFuturesUgovor2() {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setId(1L);
        futuresCreateUpdateRequest.setOznaka("CONFH2022");
        futuresCreateUpdateRequest.setOpis("Some random future");
        futuresCreateUpdateRequest.setBerzaOznaka("EUREX");
        futuresCreateUpdateRequest.setContractSize(0.0);
        futuresCreateUpdateRequest.setMaintenanceMargin(0.0);
        futuresCreateUpdateRequest.setContractUnit("unit");
        futuresCreateUpdateRequest.setSettlementDate(new Date());

        FuturesUgovori future = new FuturesUgovori();
        future.setCustom(true);

        when(berzaRepository.findBerzaByOznakaBerze("EUREX")).thenReturn(new Berza());
        when(futuresUgovoriRepository.findById(1L)).thenReturn(Optional.of(future));

        berzaService.createUpdateFuturesUgovor(futuresCreateUpdateRequest);
    }

    @Test
    void testCreateUpdateFuturesUgovor3() {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setId(1L);
        futuresCreateUpdateRequest.setOznaka("CONFH2022");
        futuresCreateUpdateRequest.setOpis("Some random future");
        futuresCreateUpdateRequest.setBerzaOznaka("EUREX");
        futuresCreateUpdateRequest.setContractSize(0.0);
        futuresCreateUpdateRequest.setMaintenanceMargin(0.0);
        futuresCreateUpdateRequest.setContractUnit("unit");
        futuresCreateUpdateRequest.setSettlementDate(new Date());

        FuturesUgovori future = new FuturesUgovori();
        future.setCustom(false);

        when(berzaRepository.findBerzaByOznakaBerze("EUREX")).thenReturn(new Berza());
        when(futuresUgovoriRepository.findById(1L)).thenReturn(Optional.of(future));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateFuturesUgovor(futuresCreateUpdateRequest);
        });

        String expectedMessage = "futures contract is not custom";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateFuturesUgovor4() {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setId(1L);
        futuresCreateUpdateRequest.setOznaka("CONFH2022");
        futuresCreateUpdateRequest.setOpis("Some random future");
        futuresCreateUpdateRequest.setBerzaOznaka("EUREX");
        futuresCreateUpdateRequest.setContractSize(0.0);
        futuresCreateUpdateRequest.setMaintenanceMargin(0.0);
        futuresCreateUpdateRequest.setContractUnit("unit");
        futuresCreateUpdateRequest.setSettlementDate(new Date());

        FuturesUgovori future = new FuturesUgovori();
        future.setCustom(false);

        when(berzaRepository.findBerzaByOznakaBerze("EUREX")).thenReturn(new Berza());
        when(futuresUgovoriRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateFuturesUgovor(futuresCreateUpdateRequest);
        });

        String expectedMessage = "futures contract not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateFuturesUgovor5() {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setId(1L);
        futuresCreateUpdateRequest.setOznaka("CONFH2022");
        futuresCreateUpdateRequest.setOpis("Some random future");
        futuresCreateUpdateRequest.setBerzaOznaka("EUREX");
        futuresCreateUpdateRequest.setContractSize(0.0);
        futuresCreateUpdateRequest.setMaintenanceMargin(0.0);
        futuresCreateUpdateRequest.setContractUnit("unit");
        futuresCreateUpdateRequest.setSettlementDate(new Date());

        FuturesUgovori future = new FuturesUgovori();
        future.setCustom(false);

        when(berzaRepository.findBerzaByOznakaBerze("EUREX")).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateFuturesUgovor(futuresCreateUpdateRequest);
        });

        String expectedMessage = "exchange not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testCreateUpdateFuturesUgovor6() {
        FuturesCreateUpdateRequest futuresCreateUpdateRequest = new FuturesCreateUpdateRequest();
        futuresCreateUpdateRequest.setId(1L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            berzaService.createUpdateFuturesUgovor(futuresCreateUpdateRequest);
        });

        String expectedMessage = "bad request";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

}
