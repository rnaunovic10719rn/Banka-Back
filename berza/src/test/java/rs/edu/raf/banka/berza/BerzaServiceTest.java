package rs.edu.raf.banka.berza;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.repository.UserAccountRepository;
import rs.edu.raf.banka.berza.response.MakeOrderResponse;
import rs.edu.raf.banka.berza.response.OrderStatusResponse;
import rs.edu.raf.banka.berza.service.impl.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BerzaServiceTest {

    @InjectMocks
    BerzaService berzaService;

    @Mock
    OrderService orderService;

    @Mock
    UserAccountRepository userAccountRepository;

    @Mock
    AkcijePodaciService akcijePodaciService;

    @Mock
    AkcijePodaciDto akcijePodaciDto;

    @Mock
    BerzaRepository berzaRepository;

    @Mock
    TransakcijaService transakcijaService;

    @Mock
    FuturesUgovoriRepository futuresUgovoriRepository;

    @Mock
    ForexRepository forexRepository;

    @Mock
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    ForexPodaciService forexPodaciService;

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
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<Double>());

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
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<Double>());

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
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<Double>());

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
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<Double>());

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
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<Double>());

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
        when(transakcijaService.findPriceActionBuy(any())).thenReturn(new ArrayList<Double>());

        MakeOrderResponse makeOrderRes = berzaService.makeOrder(17L, "usd", "AKCIJA",
                100, "buy", 0, 1, true, false);
        assertEquals(new MakeOrderResponse("Order Successful").getMessage(),makeOrderRes.getMessage());
    }
}
