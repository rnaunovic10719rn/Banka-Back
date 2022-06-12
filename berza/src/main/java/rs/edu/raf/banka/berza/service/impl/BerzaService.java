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

    public OrderResponse makeOrder(String token, String oznakaHartije, String hartijaTipString,
                                       Integer kolicina, String action,
                                       Integer limitValue, Integer stopValue, boolean isAON, boolean isMargin){

        // Korak 1: Odredi tip hartije od vrednosti koji se trguje, odredi da li je buy ili sell akcija, odredi koji je tip ordera
        HartijaOdVrednostiType hartijaTip = HartijaOdVrednostiType.valueOf(hartijaTipString.toUpperCase()); // AKCIJA, FOREX, FUTURES_UGOVOR
        OrderAction orderAkcija = OrderAction.valueOf(action.toUpperCase()); // BUY ili SELL
        OrderType orderType = getOrderType(limitValue, stopValue); // MARKET, STOP, LIMIT, STOP_LIMIT

        // Korak 1a: Nije definisana akcija SELL za Forex, posto Forex uvek koristi par (prodajem EUR da bih kupio USD)
        if(hartijaTip.equals(HartijaOdVrednostiType.FOREX) && orderAkcija.equals(OrderAction.SELL)) {
            return new OrderResponse(MessageUtils.ORDER_REJECTED);
        }

        // Korak 1b: Inicijalizacija potrebnih promeniljivih
        Long hartijaId = -1L;
        Long berza = -1L;
        Double ask = 0.0;
        Double bid = 0.0;

        // Korak 2: Preuzmi podatke o hartiji o vrednosti
        if(hartijaTip.equals(HartijaOdVrednostiType.AKCIJA)){
            AkcijePodaciDto akcije = akcijePodaciService.getAkcijaByTicker(oznakaHartije);
            if(akcije != null) {
                hartijaId = akcije.getId();
                berza = akcije.getBerzaId();

                // NB: Ne postoje podaci o asku, bidu, uzima se trenutna cena
                ask = akcije.getPrice();
                bid = akcije.getPrice();
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)){
            FuturesPodaciDto futuresUgovori = futuresUgovoriPodaciService.getFuturesUgovor(oznakaHartije);
            if(futuresUgovori != null) {
                hartijaId = futuresUgovori.getId();
                ask = futuresUgovori.getHigh();
                bid = futuresUgovori.getHigh();
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FOREX)){
            String split[] = oznakaHartije.split(" ");
            ForexPodaciDto forex = forexPodaciService.getForexBySymbol(split[0], split[1]);
            if(forex != null) {
                hartijaId = forex.getId();
                ask = forex.getAsk();
                bid = forex.getBid();
            }
        }

        // Korak 2a: Proveri ispravnost hartije od vrednosti
        if(hartijaId == -1L)
            return new OrderResponse(MessageUtils.ERROR);
        // TODO: Refactor?
        OrderService.berzaId = berza;

        // Korak 3: Izracunaj ukupnu cenu i proviziju
        Double ukupnaCena = getPrice(ask, bid, orderAkcija) * kolicina;
        Double provizija = getCommission(ukupnaCena, orderType);

        // Korak 4: Odredi order status, tj. da li order mora da bude approvovan ili je automatski approvovan
        OrderStatus status = getOrderStatus(token, ukupnaCena);

        // Korak 4a: Uzmi ID korisnika kako bi mogli da vezemo porudzbinu za korisnika
        Long userId = userService.getUserByToken(token).getId();

        // Korak 5: Sacuvaj order u bazi podataka
        Order order = orderService.saveOrder(userId, hartijaId, hartijaTip, kolicina, orderAkcija, ukupnaCena,
                provizija, orderType, isAON, isMargin, oznakaHartije, status, ask, bid);

        // Korak 6: Pokreni izvrsavanje ordera ako je automatski approvovan (inace ce izvrasavenje poceti tek da se approvuje)
        if(status == OrderStatus.APPROVED)
            orderService.executeOrder(order.getId());

        // Korak 7: Vrati poruku da je order primljen
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
        } else { // SELL
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
            Double presostaoLimit = user.getLimit() - user.getLimitUsed();
            if(user.isNeedsSupervisorPermission() || (presostaoLimit - price < 0))
                return OrderStatus.ON_HOLD;
        }

        return OrderStatus.APPROVED;
    }

}
