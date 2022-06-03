package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.service.impl.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Test
    void testGetOrders() {
        Order order = new Order();
        order.setOrderType(OrderType.LIMIT_ORDER);
        when(orderRepository.findAll()).thenReturn(List.of(order));
        assertEquals(OrderType.LIMIT_ORDER, orderService.getOrders("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M", "", null).get(0).getOrderType());
    }

    @Test
    void testSaveOrder() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.AKCIJA;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.SELL;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = true;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 0.0;

        Order order = new Order();
        order.setUserId(userAccount);
        order.setHartijaOdVrednostiId(hartijaOdVrednostiId);
        order.setHartijaOdVrednosti(hartijaOdVrednostiType);
        order.setKolicina(kolicina);
        order.setOrderAction(orderAction);
        order.setUkupnaCena(ukupnaCena);
        order.setProvizija(provizija);
        order.setOrderType(orderType);
        order.setAON(isAON);
        order.setMargin(isMargin);
        order.setOznakaHartije(oznakaHartije);
        order.setAsk(ask);
        order.setBid(bid);

        when(orderRepository.save(order)).thenReturn(order);

        assertEquals(OrderAction.SELL, orderService.saveOrder(userAccount, hartijaOdVrednostiId,
                hartijaOdVrednostiType,kolicina, orderAction,ukupnaCena,provizija,
                orderType, isAON, isMargin, oznakaHartije, OrderStatus.APPROVED, ask, bid).getOrderAction());
    }
}
