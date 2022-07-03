package rs.edu.raf.banka.berza;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.repository.OrderRepository;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.requests.TransakcijaRequest;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.response.MarginTransakcijaResponse;
import rs.edu.raf.banka.berza.response.TransakcijaResponse;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;
import rs.edu.raf.banka.berza.service.impl.OrderService;
import rs.edu.raf.banka.berza.service.impl.PriceService;
import rs.edu.raf.banka.berza.service.impl.UserService;
import rs.edu.raf.banka.berza.service.remote.TransakcijaService;
import rs.edu.raf.banka.berza.utils.HttpUtils;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserService userService;

    @Mock
    TransakcijaService transakcijaService;

    @Mock
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    PriceService priceService;

    @Mock
    EntityManager entityManager;

    @Test
    void testGetOrders1() {
        Order order = new Order();

        order.setOrderType(OrderType.LIMIT_ORDER);
        UserDto user = new UserDto();
        when(userService.getUserByToken(any())).thenReturn(user);
        when(userService.getUserRoleByToken(any())).thenReturn("ROLE_ADMIN");
        when(orderRepository.findOrderByUserId(any())).thenReturn(List.of(order));
        assertEquals(OrderType.LIMIT_ORDER, orderService.getOrders("token").get(0).getOrderType());
    }
    @Test
    void testGetOrders12() {
        Order order = new Order();

        order.setOrderType(OrderType.LIMIT_ORDER);
        UserDto user = new UserDto();
        when(userService.getUserByToken(any())).thenReturn(user);
        when(userService.getUserRoleByToken(any())).thenReturn("ROLE_ADMINn");
        when(orderRepository.findAll()).thenReturn(List.of(order));
        assertEquals(OrderType.LIMIT_ORDER, orderService.getOrders("token").get(0).getOrderType());
    }

    @Test
    void testGetOrders() {
        Order order = new Order();

        order.setOrderType(OrderType.LIMIT_ORDER);
        UserDto user = new UserDto();
        when(userService.getUserByToken(any())).thenReturn(user);
        when(userService.getUserRoleByToken(any())).thenReturn("ROLE_AGENT");
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        when(orderRepository.findOrderByUserIdAndDoneAndOrderStatus(anyLong(), any(), any())).thenReturn(orders);
        assertEquals(OrderType.LIMIT_ORDER, orderService.getOrders("token","APPROVED",true).get(0).getOrderType());
    }

    @Test
    void testGetOrders2() {
        Order order = new Order();

        order.setOrderType(OrderType.LIMIT_ORDER);
        UserDto user = new UserDto();
        when(userService.getUserByToken(any())).thenReturn(user);
        when(userService.getUserRoleByToken(any())).thenReturn("ROLE_ADMIN");
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        when(orderRepository.findOrderByOrderStatusAndDone(any(),any())).thenReturn(orders);
        assertEquals(OrderType.LIMIT_ORDER, orderService.getOrders("token","APPROVED",true).get(0).getOrderType());
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

        order.setBerza(berza);

        when(orderRepository.save(any())).thenReturn(order);

        var request = new OrderRequest();
        request.setSymbol(oznakaHartije);
        request.setHartijaOdVrednostiTip(hartijaOdVrednostiType.toString());
        request.setAkcija("buy");
        request.setKolicina(kolicina);
        request.setAllOrNoneFlag(isAON);
        request.setMarginFlag(isMargin);

        UserDto user = new UserDto();
        user.setUsername("username");

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";
        when(userService.getUserByToken(any())).thenReturn(user);

        TransakcijaResponse tr = new TransakcijaResponse();
        when(transakcijaService.commitTransaction(any(), any())).thenReturn(tr);

        assertEquals(OrderAction.SELL, orderService.saveOrder(token, request, userAccount, berza, hartijaOdVrednostiId,
                hartijaOdVrednostiType, orderAction,ukupnaCena,provizija,
                orderType, OrderStatus.APPROVED).getOrderAction());
    }

    @Test
    void testApproveOrderROLE_SUPERVISOR(){
        String userRole = UserRole.ROLE_SUPERVISOR.toString();
        Long id = 1L;

        Order order = new Order();
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.FUTURES_UGOVOR);
        order.setHartijaOdVrednostiId(1L);
        when(orderRepository.getById(any())).thenReturn(order);
        when(futuresUgovoriPodaciService.isRelevant(order.getHartijaOdVrednostiId())).thenReturn(true);
        assertEquals(new ApproveRejectOrderResponse(MessageUtils.ORDER_APPROVED), orderService.approveOrder(userRole, id));
    }

    @Test
    void testApproveOrderROLEAGENT(){
        String userRole = UserRole.ROLE_AGENT.toString();
        Long id = 1L;
        assertEquals(new ApproveRejectOrderResponse(MessageUtils.REQUIRED_PERMISSION), orderService.approveOrder(userRole, id));
    }

    @Test
    void testApproveOrderROLE_SUPERVISORreject(){
        String userRole = UserRole.ROLE_SUPERVISOR.toString();
        Long id = 1L;

        Order order = new Order();
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.FUTURES_UGOVOR);
        order.setHartijaOdVrednostiId(1L);
        when(orderRepository.getById(any())).thenReturn(order);
        when(futuresUgovoriPodaciService.isRelevant(order.getHartijaOdVrednostiId())).thenReturn(false);
        assertEquals(new ApproveRejectOrderResponse(MessageUtils.ORDER_REJECTED), orderService.approveOrder(userRole, id));
    }

    @Test
    void testApproveOrderROLEAGENTreject(){
        String userRole = UserRole.ROLE_AGENT.toString();
        Long id = 1L;
        assertEquals(new ApproveRejectOrderResponse(MessageUtils.REQUIRED_PERMISSION), orderService.rejectOrder(userRole, id));
    }

    @Test
    void testSaveOrderForex() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FOREX;
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

        order.setBerza(berza);

        when(orderRepository.save(any())).thenReturn(order);

        var request = new OrderRequest();
        request.setSymbol(oznakaHartije);
        request.setHartijaOdVrednostiTip(hartijaOdVrednostiType.toString());
        request.setAkcija("buy");
        request.setKolicina(kolicina);
        request.setAllOrNoneFlag(isAON);
        request.setMarginFlag(isMargin);

        UserDto user = new UserDto();
        user.setUsername("username");

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";
        when(userService.getUserByToken(any())).thenReturn(user);

        TransakcijaResponse tr = new TransakcijaResponse();
        when(transakcijaService.commitTransaction(any(), any())).thenReturn(tr);

        assertEquals(OrderAction.SELL, orderService.saveOrder(token, request, userAccount, berza, hartijaOdVrednostiId,
                hartijaOdVrednostiType, orderAction,ukupnaCena,provizija,
                orderType, OrderStatus.APPROVED).getOrderAction());
    }

    @Test
    void testGetTransakcijeForOrderAKCIJA() {
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
        Boolean lastSegment = true;

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
        order.setId(1L);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);

        assertEquals(1L, orderService.getTransakcijeForOrder(order, kolicina, lastSegment).get(0).getOrderId());
    }

    @Test
    void testGetTransakcijeForOrderAKCIJABuy() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.AKCIJA;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 0.0;
        Boolean lastSegment = true;

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
        order.setId(1L);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);

        assertEquals(1L, orderService.getTransakcijeForOrder(order, kolicina, lastSegment).get(0).getOrderId());
    }

    @Test
    void testGetTransakcijeForOrderFOREX() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FOREX;
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
        Boolean lastSegment = true;

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
        order.setId(1L);
        order.setHartijaOdVrednostiSymbol("EUR USD");

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);

        assertEquals(1L, orderService.getTransakcijeForOrder(order, kolicina, lastSegment).get(0).getOrderId());
    }

    @Test
    void testGetMarginTransakcijaForOrderBUY() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FOREX;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 0.0;
        Boolean lastSegment = true;

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
        order.setId(1L);
        order.setHartijaOdVrednostiSymbol("EUR USD");

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);

        assertEquals(1L, orderService.getMarginTransakcijaForOrder(order, kolicina).getOrderId());

    }

    @Test
    void testGetMarginTransakcijaForOrderSELL() {
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
        order.setId(1L);
        order.setHartijaOdVrednostiSymbol("EUR USD");

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);

        assertEquals(1L, orderService.getMarginTransakcijaForOrder(order, kolicina).getOrderId());

    }

    @Test
    void testSaveOrderBUYAKCIJA() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.AKCIJA;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
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

        order.setBerza(berza);

        when(orderRepository.save(any())).thenReturn(order);

        var request = new OrderRequest();
        request.setSymbol(oznakaHartije);
        request.setHartijaOdVrednostiTip(hartijaOdVrednostiType.toString());
        request.setAkcija("buy");
        request.setKolicina(kolicina);
        request.setAllOrNoneFlag(isAON);
        request.setMarginFlag(isMargin);

        UserDto user = new UserDto();
        user.setUsername("username");

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";
        when(userService.getUserByToken(any())).thenReturn(user);

        TransakcijaResponse tr = new TransakcijaResponse();
        when(transakcijaService.commitTransaction(any(), any())).thenReturn(tr);

        assertEquals(OrderAction.BUY, orderService.saveOrder(token, request, userAccount, berza, hartijaOdVrednostiId,
                hartijaOdVrednostiType, orderAction,ukupnaCena,provizija,
                orderType, OrderStatus.APPROVED).getOrderAction());
    }

    @Test
    void testSaveOrderBUYFuture() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
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

        order.setBerza(berza);

        when(orderRepository.save(any())).thenReturn(order);

        var request = new OrderRequest();
        request.setSymbol(oznakaHartije);
        request.setHartijaOdVrednostiTip(hartijaOdVrednostiType.toString());
        request.setAkcija("buy");
        request.setKolicina(kolicina);
        request.setAllOrNoneFlag(isAON);
        request.setMarginFlag(isMargin);

        UserDto user = new UserDto();
        user.setUsername("username");

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";
        when(userService.getUserByToken(any())).thenReturn(user);

        TransakcijaResponse tr = new TransakcijaResponse();
        when(transakcijaService.commitTransaction(any(), any())).thenReturn(tr);

        assertEquals(OrderAction.BUY, orderService.saveOrder(token, request, userAccount, berza, hartijaOdVrednostiId,
                hartijaOdVrednostiType, orderAction,ukupnaCena,provizija,
                orderType, OrderStatus.APPROVED).getOrderAction());
    }

    @Test
    void testSaveOrderSELLFuture() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
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

        order.setBerza(berza);

        when(orderRepository.save(any())).thenReturn(order);

        var request = new OrderRequest();
        request.setSymbol(oznakaHartije);
        request.setHartijaOdVrednostiTip(hartijaOdVrednostiType.toString());
        request.setAkcija("buy");
        request.setKolicina(kolicina);
        request.setAllOrNoneFlag(isAON);
        request.setMarginFlag(isMargin);

        UserDto user = new UserDto();
        user.setUsername("username");

        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbixST0xFX0dMX0FETUlOIiwicGVybWlzc2lvbnMiOlsiQ1JFQVRFX1VTRVIiLCJERUxFVEVfVVNFUiIsIkVESVRfVVNFUiIsIkxJU1RfVVNFUlMiLCJNQU5BR0VfQUdFTlRTIiwiTVlfRURJVCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL2xvZ2luIn0.K1ZdSiUWFXISTJvLI5WvFCcje9vWTWKxxyJmMBTe03M";
        when(userService.getUserByToken(any())).thenReturn(user);

        TransakcijaResponse tr = new TransakcijaResponse();
        when(transakcijaService.commitTransaction(any(), any())).thenReturn(tr);

        assertEquals(OrderAction.BUY, orderService.saveOrder(token, request, userAccount, berza, hartijaOdVrednostiId,
                hartijaOdVrednostiType, orderAction,ukupnaCena,provizija,
                orderType, OrderStatus.APPROVED).getOrderAction());
    }

    @Test
    void testGetBerzaStatus() {
        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());
        assertNotNull(orderService.getBerzaStatus(berza));
    }

    @Test
    void testCanExecuteTransactionSellLimitOrder() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(1);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionSellStopOrder() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.STOP_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(1);
        order.setStopValue(10);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionSell(order));
    }
    @Test
    void testCanExecuteTransactionSellStopLimitOrder() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.STOP_LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(1);
        order.setStopValue(10);
        order.setPredvidjenaCena(12.0);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionSellDefault() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.MARKET_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(1);
        order.setStopValue(10);
        order.setPredvidjenaCena(12.0);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionSell(order));
    }

    @Test
    void testCanExecuteTransactionBUYDefault() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.MARKET_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(1);
        order.setStopValue(10);
        order.setPredvidjenaCena(12.0);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionBuy(order));
    }

    @Test
    void testCanExecuteTransactionBUYLimit() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(11);
        order.setStopValue(10);
        order.setPredvidjenaCena(12.0);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionBuy(order));
    }

    @Test
    void testCanExecuteTransactionBUYSTOP() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.STOP_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 11.0;
        Double bid = 2.0;

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
        order.setLimitValue(1);
        order.setStopValue(10);
        order.setPredvidjenaCena(12.0);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionBuy(order));
    }
    @Test
    void testCanExecuteTransactionBUYSTOPLimit() {
        Long userAccount = 1L;
        Long hartijaOdVrednostiId = 1L;
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        Integer kolicina = 1;
        OrderAction orderAction = OrderAction.BUY;
        Double ukupnaCena = 1.0;
        Double provizija = 1.0;
        OrderType orderType = OrderType.STOP_LIMIT_ORDER;
        boolean isAON = true;
        boolean isMargin = false;
        String oznakaHartije = "usd";
        Double ask = 1.0;
        Double bid = 2.0;

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
        order.setLimitValue(10);
        order.setStopValue(10);
        order.setPredvidjenaCena(1.0);

        Long berzaId = 1L;
        Berza berza = new Berza();
        berza.setId(berzaId);
        berza.setOpenTime("00:00:00");
        berza.setCloseTime("23:00:00");
        berza.setOrderi(new ArrayList<>());

        order.setBerza(berza);
        assertTrue(orderService.canExecuteTransactionBuy(order));
    }

    @Test
    void testExecuteTransaction1() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(null);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(0);
        order.setStopValue(0);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(true);
        order.setMargin(false);
        order.setAsk(1000.0);
        order.setBid(1000.0);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);
        when(priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol())).thenReturn(askBidPriceDto);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)) {
            utilities.when(HttpUtils::retryTemplate).thenReturn(null);

            orderService.executeTransaction(order);

            assertEquals(order.getDone(), true);
        }
    }

    @Test
    void testExecuteTransaction2() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(null);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.MARKET_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(0);
        order.setStopValue(0);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(false);
        order.setMargin(true);
        order.setAsk(1000.0);
        order.setBid(1000.0);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);
        when(priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol())).thenReturn(askBidPriceDto);
        when(transakcijaService.commitMarginsTransaction(any(), any())).thenReturn(new MarginTransakcijaResponse());

        orderService.executeTransaction(order);
        assertEquals(order.getDone(), true);
    }

    @Test
    void testExecuteTransaction3() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(null);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(500);
        order.setStopValue(0);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(true);
        order.setMargin(false);
        order.setAsk(1000.0);
        order.setBid(1000.0);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);
        when(priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol())).thenReturn(askBidPriceDto);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)) {
            utilities.when(HttpUtils::retryTemplate).thenReturn(null);

            orderService.executeTransaction(order);

            assertEquals(order.getDone(), false);
        }
    }

    @Test
    void testExecuteTransaction4() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(null);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.LIMIT_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(1500);
        order.setStopValue(0);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(true);
        order.setMargin(false);
        order.setAsk(1000.0);
        order.setBid(1000.0);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);
        when(priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol())).thenReturn(askBidPriceDto);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)) {
            utilities.when(HttpUtils::retryTemplate).thenReturn(null);

            orderService.executeTransaction(order);

            assertEquals(order.getDone(), true);
        }
    }

    @Test
    void testExecuteTransaction5() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(null);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.STOP_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(0);
        order.setStopValue(1500);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(true);
        order.setMargin(false);
        order.setAsk(1000.0);
        order.setBid(1000.0);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);
        when(priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol())).thenReturn(askBidPriceDto);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)) {
            utilities.when(HttpUtils::retryTemplate).thenReturn(null);

            orderService.executeTransaction(order);

            assertEquals(order.getDone(), false);
        }
    }

    @Test
    void testExecuteTransaction6() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(null);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.STOP_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(0);
        order.setStopValue(500);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(true);
        order.setMargin(false);
        order.setAsk(1000.0);
        order.setBid(1000.0);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);
        when(priceService.getAskBidPrice(order.getHartijaOdVrednosti(), order.getHartijaOdVrednostiSymbol())).thenReturn(askBidPriceDto);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)) {
            utilities.when(HttpUtils::retryTemplate).thenReturn(null);

            orderService.executeTransaction(order);

            assertEquals(order.getDone(), true);
        }
    }

    @Test
    void testExecuteTransaction7() {
        Berza berza = new Berza();
        berza.setOpenTime("00:00");
        berza.setCloseTime("00:01");

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUsername("admin");
        order.setBerza(berza);
        order.setOrderAction(OrderAction.BUY);
        order.setOrderType(OrderType.STOP_ORDER);
        order.setOrderStatus(OrderStatus.APPROVED);
        order.setHartijaOdVrednosti(HartijaOdVrednostiType.AKCIJA);
        order.setHartijaOdVrednostiId(1L);
        order.setHartijaOdVrednostiSymbol("AAPL");
        order.setKolicina(10);
        order.setPreostalaKolicina(10);
        order.setBackoff(0);
        order.setLimitValue(0);
        order.setStopValue(500);
        order.setPredvidjenaCena(1000.0);
        order.setProvizija(0.0);
        order.setAON(true);
        order.setMargin(false);
        order.setAsk(1000.0);
        order.setBid(1000.0);
        order.setBackoff(-1);

        AskBidPriceDto askBidPriceDto = new AskBidPriceDto();
        askBidPriceDto.setHartijaId(1L);
        askBidPriceDto.setAsk(1000.0);
        askBidPriceDto.setBid(1000.0);
        askBidPriceDto.setBerza(null);

        ArrayList<TransakcijaRequest> transakcijaRequests = new ArrayList<>();
        transakcijaRequests.add(new TransakcijaRequest());

        when(entityManager.find(Order.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)) {
            utilities.when(HttpUtils::retryTemplate).thenReturn(null);

            orderService.executeTransaction(order);

            assertEquals(order.getDone(), false);
        }
    }

    @Test
    void testExecuteOrders() {
        List<Order> orders = new ArrayList<>();
        Order o = new Order();
        o.setOrderStatus(OrderStatus.REJECTED);
        orders.add(o);

        when(orderRepository.findOrderByDoneFalse()).thenReturn(orders);

        orderService.executeOrder();
        assertEquals(orders.get(0).getDone(), false);
    }

}
