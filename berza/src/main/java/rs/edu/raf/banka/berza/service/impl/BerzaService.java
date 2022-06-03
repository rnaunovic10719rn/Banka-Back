package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.*;
import rs.edu.raf.banka.berza.repository.*;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.response.OrderStatusResponse;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BerzaService {

    private BerzaRepository berzaRepository;
    private AkcijeRepository akcijeRepository;
    private OrderService orderService;
    private TransakcijaService transakcijaService;
    private AkcijePodaciService akcijePodaciService;
    private ForexPodaciService forexPodaciService;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    private UserService userService;


    @Autowired
    public BerzaService(BerzaRepository berzaRepository, AkcijeRepository akcijeRepository,
                        TransakcijaService transakcijaService, OrderService orderService, AkcijePodaciService akcijePodaciService,
                        ForexPodaciService forexPodaciService, FuturesUgovoriPodaciService futuresUgovoriPodaciService,
                        UserService userService){
        this.berzaRepository = berzaRepository;
        this.akcijeRepository = akcijeRepository;
        this.transakcijaService = transakcijaService;
        this.orderService = orderService;
        this.akcijePodaciService = akcijePodaciService;
        this.forexPodaciService = forexPodaciService;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
        this.userService = userService;
    }

    public List<Berza> findAll(){
        return berzaRepository.findAll();
    }

    public Berza findBerza(String oznaka){
        return berzaRepository.findBerzaByOznakaBerze(oznaka);
    }

    public Akcije findAkcije(String symbol){
        return akcijeRepository.findAkcijeByOznakaHartije(symbol);
    }

    public OrderResponse makeOrder(String token, Long userId, String oznakaHartije, String hartijaTipString,
                                       Integer kolicina, String action,
                                       Integer limitValue, Integer stopValue, boolean isAON, boolean isMargin){
        HartijaOdVrednostiType hartijaTip = HartijaOdVrednostiType.valueOf(hartijaTipString.toUpperCase());
        OrderAction orderAkcija = OrderAction.valueOf(action.toUpperCase());
        OrderType orderType = getOrderType(limitValue, stopValue);

        Double ask = 0.0;
        Double bid = 0.0;
        Long hartijaId = -1L;
        Long berza = -1L;
        if(hartijaTip.equals(HartijaOdVrednostiType.AKCIJA)){
            AkcijePodaciDto akcije = akcijePodaciService.getAkcijaByTicker(oznakaHartije);
  //          Akcije akcije = akcijeRepository.findAkcijeByOznakaHartije(symbol);
            if(akcije != null) {
                hartijaId = akcije.getId();
                berza = akcije.getBerzaId();
                //ne postoje podaci o asku, bidu, uzima se trnt cena
                ask = akcije.getPrice();
                bid = akcije.getPrice();
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)){
            FuturesPodaciDto futuresUgovori = futuresUgovoriPodaciService.getFuturesUgovor(oznakaHartije);
//            FuturesUgovori futuresUgovori = futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(symbol);
            if(futuresUgovori != null) {
                hartijaId = futuresUgovori.getId();
//                berza = futuresUgovori.getBerza().getId();
                ask = futuresUgovori.getHigh();
                bid = futuresUgovori.getHigh();
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FOREX)){
            String split[] = oznakaHartije.split(" ");
            ForexPodaciDto forex = forexPodaciService.getForexBySymbol(split[0], split[1]);
//            Forex forex = forexRepository.findForexByOznakaHartije(symbol);
            if(forex != null) {
                hartijaId = forex.getId();
//                berza = forex.getBerza().getId();
                ask = forex.getAsk();
                bid = forex.getBid();
            }
        }

        if(hartijaId == -1L)
            return new OrderResponse("Error");
        OrderService.berzaId = berza;

        Double ukupnaCena = getPrice(ask, bid, orderAkcija);
        Double provizija = getCommission(ukupnaCena, orderType);

        OrderStatus status = getOrderStatus(token, ukupnaCena);

        Order order = orderService.saveOrder(userId, hartijaId, hartijaTip, kolicina, orderAkcija, ukupnaCena,
                provizija, orderType, isAON, isMargin, oznakaHartije, status, ask, bid);

        if(status == OrderStatus.APPROVED)
            orderService.executeOrder(order.getId());

        return new OrderResponse(MessageUtils.ORDER_SUCCESSFUL);
    }

    public Double getPrice(Double ask, Double bid, OrderAction orderAction){
        List<Double> cene;
        Random random = new Random();

        Double toReturn;
        if(orderAction.equals(OrderAction.BUY)) {
            cene = transakcijaService.findPriceActionBuy(bid);
            if(cene.size() >= 3)
                return cene.get(random.nextInt(3));
            toReturn = bid;
        }
        else {
            cene = transakcijaService.findPriceActionBuy(ask);
            if(cene.size() >= 3)
                return cene.get(random.nextInt(3));
            toReturn = ask;
        }

        return toReturn;
    }

    public Double getCommission(Double price, OrderType orderType) {
        if(orderType.equals(OrderType.MARKET_ORDER))
            return Math.min(0.14 * price, 7);
        return Math.min(0.24 * price, 12);
    }

    private OrderType getOrderType(Integer limitValue, Integer stopValue) {
        if(limitValue > 0 && stopValue > 0)
            return OrderType.STOP_LIMIT_ORDER;
        else if(limitValue > 0)
            return OrderType.LIMIT_ORDER;
        else if(stopValue > 0)
            return OrderType.STOP_ORDER;
        return OrderType.MARKET_ORDER;
    }

    private OrderStatus getOrderStatus(String token, double price) {
        UserRole role = UserRole.valueOf(userService.getUserRoleByToken(token));

        if(role.equals(UserRole.ROLE_AGENT)) {
            UserDto user = userService.getUserByToken(token);
            if(user.isNeedsSupervisorPermission() && user.getLimitUsed() == user.getLimit() && user.getLimit() < price)
                return OrderStatus.ON_HOLD;
        }

        return OrderStatus.APPROVED;
    }

}
