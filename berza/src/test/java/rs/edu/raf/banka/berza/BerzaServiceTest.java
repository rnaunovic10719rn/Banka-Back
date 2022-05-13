package rs.edu.raf.banka.berza;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.*;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.response.MakeOrderResponse;
import rs.edu.raf.banka.berza.service.impl.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BerzaServiceTest {

    @InjectMocks
    BerzaService berzaService;

    @Mock
    OrderService orderService;

    @Mock
    AkcijePodaciService akcijePodaciService;

    @Mock
    BerzaRepository berzaRepository;

    @Mock
    TransakcijaService transakcijaService;


    @Mock
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    ForexPodaciService forexPodaciService;

    @Mock
    AkcijeRepository akcijeRepository;


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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(17L, 1L, HartijaOdVrednostiType.AKCIJA,100,OrderAction.BUY,
                10.0,1.4000000000000001,OrderType.MARKET_ORDER,true,false, "usd")).thenReturn(order);
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd", "AKCIJA",
                100, "buy", 0, 0, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testMakeOrderFuturesUgovor(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setUkupnaCena(101.0);
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
        when(orderService.saveOrder(17L, 1L, HartijaOdVrednostiType.FUTURES_UGOVOR,100,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_LIMIT_ORDER,true,false,"usd")).thenReturn(order);
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd", "FUTURES_UGOVOR",
                100, "buy", 1, 1, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testMakeOrderForex(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setUkupnaCena(101.0);
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

        when(forexPodaciService.getForexBySymbol(any(), any())).thenReturn(forex);
//        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(17L, 1L, HartijaOdVrednostiType.FOREX,100,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_LIMIT_ORDER,true,false, "usd lol")).thenReturn(order);
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd lol", "FOREX",
                100, "buy", 1, 1, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(17L, 1L, HartijaOdVrednostiType.AKCIJA,100,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_LIMIT_ORDER,true,false, "usd")).thenReturn(order);
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd", "AKCIJA",
                100, "buy", 1, 1, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(17L, 1L, HartijaOdVrednostiType.AKCIJA,100,OrderAction.BUY,
                10.0,2.4,OrderType.LIMIT_ORDER,true,false, "usd")).thenReturn(order);
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd", "AKCIJA",
                100, "buy", 1, 0, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);

        Berza berza = new Berza();
        berza.setId(2L);
        berza.setOpenTime("09:00:00");
        berza.setCloseTime("23:00:00");

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(akcije);
        when(berzaRepository.findBerzaById(2L)).thenReturn(berza);
        when(orderService.saveOrder(17L, 1L, HartijaOdVrednostiType.AKCIJA,100,OrderAction.BUY,
                10.0,2.4,OrderType.STOP_ORDER,true,false, "usd")).thenReturn(order);
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd", "AKCIJA",
                100, "buy", 0, 1, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }

    @Test
    void testFindAll() {
        Berza berza1 = new Berza();
        Berza berza2 = new Berza();
        when(berzaRepository.findAll()).thenReturn(List.of(new Berza[]{berza1, berza2}));
        assertEquals(2, berzaService.findAll().size());
    }

    @Test
    void testAddOrderToBerza(){
        Order order = new Order();
        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setOrderi(new ArrayList<>());
        when(berzaRepository.findBerzaById(berzaId)).thenReturn(berza);
        when(berzaRepository.save(berza)).thenReturn(berza);
        berzaService.addOrderToBerza(order,berzaId);
        assertEquals(1,berza.getOrderi().size());
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
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionSell(order, ask));
    }

    @Test
    void testCanExecuteTransactionSellSTOP_LIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setOrderType(OrderType.STOP_LIMIT_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionSell(order, ask));
    }

    @Test
    void testCanExecuteTransactionSellSTOP_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setOrderType(OrderType.STOP_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionSell(order, ask));
    }

    @Test
    void testCanExecuteTransactionSellMARKET_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 200.00;
        int limitValue = 100;
        Double ask = 50.00;
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionSell(order, ask));
    }

    @Test
    void testCanExecuteTransactionButLIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionBuy(order, bid));
    }

    @Test
    void testCanExecuteTransactionButSTOP_LIMIT_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setOrderType(OrderType.STOP_LIMIT_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionBuy(order, bid));
    }

    @Test
    void testCanExecuteTransactionButSTOP_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setOrderType(OrderType.STOP_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionBuy(order, bid));
    }

    @Test
    void testCanExecuteTransactionButMARKET_ORDER() {
        Order order = new Order();
        Double ukupnaCena = 100.00;
        int limitValue = 200;
        Double bid = 500.00;
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setUkupnaCena(ukupnaCena);
        order.setLimitValue(limitValue);
        assertTrue(berzaService.canExecuteTransactionBuy(order, bid));
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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);
        order.setAON(true);
        Double ask = 1.00;
        Double bid = 1.00;

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());
        when(berzaRepository.findBerzaById(1L)).thenReturn(berza);

        assertEquals("OK", berzaService.executeTransaction(berzaId, order,ask,bid).getMessage());
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
        order.setUkupnaCena(200.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 1.00;
        Double bid = 1.00;

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());
        when(berzaRepository.findBerzaById(1L)).thenReturn(berza);

        assertEquals("You can't proceed this action.", berzaService.executeTransaction(berzaId, order,ask,bid).getMessage());
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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 1.00;
        Double bid = 1.00;

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());
        when(berzaRepository.findBerzaById(1L)).thenReturn(berza);

        assertEquals("You can't proceed this action.", berzaService.executeTransaction(berzaId, order,ask,bid).getMessage());
    }

    @Test
    void testGetPrice(){
        Order order = new Order();
        order.setKolicina(100);
        order.setOrderAction(OrderAction.SELL);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setUkupnaCena(101.0);
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
        order.setUkupnaCena(101.0);
        order.setLimitValue(105);
        order.setAON(false);
        Double ask = 12.00;
        Double bid = 1.00;
        List<Double> cene = new ArrayList<>(List.of(1.0, 2.0, 3.0));
        when(transakcijaService.findPriceActionBuy(ask)).thenReturn(cene);
        assertTrue(cene.contains(berzaService.getPrice(ask, bid, OrderAction.SELL)));
    }
}
