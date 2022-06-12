package rs.edu.raf.banka.berza;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.Before;
import org.hibernate.service.spi.InjectService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.*;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.service.impl.*;
import rs.edu.raf.banka.berza.service.remote.TransakcijaService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BerzaServiceTest {


    @InjectMocks
    BerzaService berzaService;

    @Mock
    AkcijePodaciService akcijePodaciService;

    @Mock
    BerzaRepository berzaRepository;

    @Mock
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    ForexPodaciService forexPodaciService;

    @Mock
    AkcijeRepository akcijeRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserService userService;

    @InjectMocks
    OrderService orderService;

    @Mock
    PriceService priceService;

    @Mock
    TransakcijaService transakcijaService;

    @BeforeEach
    public void setUp() {
        orderService = Mockito.spy(new OrderService(orderRepository, futuresUgovoriPodaciService, priceService, userService, transakcijaService));
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void testMakeOrderAkcijaMarketOrder(){
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

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        order.setBerza(berza);

        var user = new UserDto();
        user.setId(17);
        user.setRoleName(UserRole.ROLE_ADMIN.toString());

        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.AKCIJA.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        //request.setLimitValue();
        //request.setLimitValue();
        //request.setStopValue()
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";


        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(orderService.saveOrder(token, request, user.getId(), null,1L, HartijaOdVrednostiType.AKCIJA,OrderAction.BUY,
                1000.0,7.0,OrderType.MARKET_ORDER, OrderStatus.APPROVED)).thenReturn(order);

        when(userService.getUserRoleByToken(token)).thenReturn(user.getRoleName());
        when(userService.getUserByToken(token)).thenReturn(user);

        OrderResponse makeOrderRes = berzaService.makeOrder(token, request);
        assertEquals(new OrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testMakeOrderFuturesUgovor(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        FuturesPodaciDto futuresUgovori = new FuturesPodaciDto();
        futuresUgovori.setId(1L);
//        futuresUgovori.setBerza(berza);
        futuresUgovori.setHigh(10.0);

        when(futuresUgovoriPodaciService.getFuturesUgovor(any())).thenReturn(futuresUgovori);
//        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);

        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.FUTURES_UGOVOR.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        //request.setLimitValue();
        //request.setLimitValue();
        //request.setStopValue()
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";

        when(orderService.saveOrder(token, request,1L, berza,1L, HartijaOdVrednostiType.FUTURES_UGOVOR,OrderAction.BUY,
                1000.0,2.4,OrderType.STOP_LIMIT_ORDER, OrderStatus.APPROVED)).thenReturn(order);
        OrderResponse makeOrderRes = berzaService.makeOrder(token, request);
        assertEquals(new OrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testMakeOrderForex(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        ForexPodaciDto forex = new ForexPodaciDto();
        forex.setId(1L);
//        forex.setBerza(berza);
        forex.setAsk(10.0);
        forex.setBid(10.0);

        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.FOREX.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        //request.setLimitValue();
        //request.setLimitValue();
        //request.setStopValue()
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);


        when(forexPodaciService.getForexBySymbol(any(), any())).thenReturn(forex);
//        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";

        when(orderService.saveOrder(token, request, 1L, berza,1L, HartijaOdVrednostiType.FOREX,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_LIMIT_ORDER, OrderStatus.APPROVED)).thenReturn(order);

        OrderResponse makeOrderRes = berzaService.makeOrder(token, request);
        assertEquals(new OrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testMakeOrderAkcijaStopLimitOrder(){
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

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.AKCIJA.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        //request.setLimitValue();
        //request.setLimitValue();
        //request.setStopValue()
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";

        when(orderService.saveOrder(token, request, 1L, berza,1L, HartijaOdVrednostiType.AKCIJA,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_LIMIT_ORDER,OrderStatus.APPROVED)).thenReturn(order);

        OrderResponse makeOrderRes = berzaService.makeOrder(token, request);
        assertEquals(new OrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testMakeOrderAkcijaLimitOrder(){
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

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");


        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.AKCIJA.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        //request.setLimitValue();
        //request.setLimitValue();
        //request.setStopValue()
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(token, request, 1L, berza,1L, HartijaOdVrednostiType.AKCIJA,OrderAction.BUY,
                10.0,2.4,OrderType.LIMIT_ORDER, OrderStatus.APPROVED)).thenReturn(order);

        OrderResponse makeOrderRes = berzaService.makeOrder(token, request);
        assertEquals(new OrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
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

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        var request = new OrderRequest();
        request.setSymbol("usd");
        request.setHartijaOdVrednostiTip(HartijaOdVrednostiType.AKCIJA.toString());
        request.setAkcija("buy");
        request.setKolicina(100);
        //request.setLimitValue();
        //request.setLimitValue();
        //request.setStopValue()
        request.setAllOrNoneFlag(true);
        request.setMarginFlag(false);

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";


        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(token, request, 17L,  berza,1L, HartijaOdVrednostiType.AKCIJA,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_ORDER, OrderStatus.APPROVED)).thenReturn(order);

        OrderResponse makeOrderRes = berzaService.makeOrder(token, request);
        assertEquals(new OrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
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
    void testCanExecuteTransactionSellLIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setAsk(ask);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(orderService.canExecuteTransactionSell(order));
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
        assertTrue(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionSellSTOP_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setAsk(ask);
        order.setOrderType(OrderType.STOP_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(orderService.canExecuteTransactionSell(order));
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
        assertTrue(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionButLIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setBid(bid);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(orderService.canExecuteTransactionBuy(order));
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
        assertTrue(orderService.canExecuteTransactionBuy(order));
    }

    @Test
    void testCanExecuteTransactionButSTOP_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setBid(bid);
        order.setOrderType(OrderType.STOP_ORDER);
        order.setPredvidjenaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(orderService.canExecuteTransactionBuy(order));
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
        assertTrue(orderService.canExecuteTransactionBuy(order));
    }


    @Test
    void testExecuteTransaction() {
        AkcijePodaciDto akcije = new AkcijePodaciDto();
        akcije.setId(1L);
        akcije.setBerzaId(2L);
        akcije.setPrice(10.0);

        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);
        order.setAON(true);
        Double ask = 1.00;
        Double bid = 1.00;
        order.setAsk(ask);
        order.setBid(bid);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());
        order.setBerza(berza);
        when(berzaRepository.findBerzaById(1L)).thenReturn(berza);

        orderService.executeTransaction(order);

        assertTrue(order.getDone());
    }

    @Test
    void testExecuteTransactionBUYFail() {
        AkcijePodaciDto akcije = new AkcijePodaciDto();
        akcije.setId(1L);
        akcije.setBerzaId(2L);
        akcije.setPrice(10.0);

        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(200.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 1.00;
        Double bid = 1.00;
        order.setAsk(ask);
        order.setBid(bid);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);

        when(berzaRepository.findBerzaById(1L)).thenReturn(berza);

        orderService.executeTransaction(order);
        assertNotEquals(true, order.getDone());
    }

    @Test
    void testExecuteTransactionSELLFail() {
        AkcijePodaciDto akcije = new AkcijePodaciDto();
        akcije.setId(1L);
        akcije.setBerzaId(2L);
        akcije.setPrice(10.0);

        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.SELL);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 1.00;
        Double bid = 1.00;
        order.setAsk(ask);
        order.setBid(bid);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());
        order.setBerza(berza);
        when(berzaRepository.findBerzaById(1L)).thenReturn(berza);

        orderService.executeTransaction(order);
        assertNotEquals(true, order.getDone());
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
    void testGetPriceCeneMoreThan3(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.SELL);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setPredvidjenaCena(101.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 12.00;
        Double bid = 1.00;
        List<Double> cene = new ArrayList<>(List.of(1.0, 2.0, 3.0));

        assertTrue(cene.contains(berzaService.getPrice(ask, bid, OrderAction.SELL)));
    }
}
