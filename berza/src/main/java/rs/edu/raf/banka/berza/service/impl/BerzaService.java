package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.BerzaDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.enums.*;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.requests.AkcijaCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.FuturesCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.utils.MessageUtils;
import rs.edu.raf.banka.berza.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BerzaService {

    private BerzaRepository berzaRepository;
    private AkcijeRepository akcijeRepository;
    private FuturesUgovoriRepository futuresUgovoriRepository;
    private OrderService orderService;
    private AkcijePodaciService akcijePodaciService;
    private ForexPodaciService forexPodaciService;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;
    private PriceService priceService;

    private UserService userService;

    @Autowired
    public BerzaService(BerzaRepository berzaRepository, AkcijeRepository akcijeRepository,
                        FuturesUgovoriRepository futuresUgovoriRepository, OrderService orderService,
                        AkcijePodaciService akcijePodaciService, ForexPodaciService forexPodaciService,
                        FuturesUgovoriPodaciService futuresUgovoriPodaciService, UserService userService,
                        PriceService priceService){
        this.berzaRepository = berzaRepository;
        this.akcijeRepository = akcijeRepository;
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.orderService = orderService;
        this.akcijePodaciService = akcijePodaciService;
        this.forexPodaciService = forexPodaciService;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
        this.userService = userService;
        this.priceService = priceService;
    }

    public List<Berza> findAll(){
        return berzaRepository.findAll();
    }

    public BerzaDto findBerza(Long id) {
        Berza berza = berzaRepository.findBerzaById(id);
        BerzaDto berzaDto = new BerzaDto();
        berzaDto.setOznakaBerze(berza.getOznakaBerze());
        berzaDto.setKodValute(berza.getValuta().getKodValute());
        return berzaDto;
    }

    public Berza findBerza(String oznaka){
        return berzaRepository.findBerzaByOznakaBerze(oznaka);
    }

    public Akcije findAkcije(String symbol){
        return akcijeRepository.findAkcijeByOznakaHartije(symbol);
    }

    public List<Akcije> findAllAkcije() {
        return akcijeRepository.findAll();
    }

    public List<FuturesUgovori> findAllFuturesUgovori() {
        return futuresUgovoriRepository.findAll();
    }

    public Akcije createUpdateAkcija(AkcijaCreateUpdateRequest request) {
        if(StringUtils.emptyString(request.getOznaka()) ||
                StringUtils.emptyString(request.getOpis()) ||
                request.getOutstandingShares() == null) {
            throw new RuntimeException("bad request");
        }

        Berza berza = null;
        if(!StringUtils.emptyString(request.getBerzaOznaka())) {
            berza = berzaRepository.findBerzaByOznakaBerze(request.getBerzaOznaka());
            if(berza == null) {
                throw new RuntimeException("exchange not found");
            }
        }

        Akcije akcija = new Akcije();
        if(request.getId() != null) {
            akcija = akcijeRepository.findAkcijeById(request.getId());
            if(akcija == null) {
                throw new RuntimeException("stock not found");
            }
            if(!akcija.getCustom()) {
                throw new RuntimeException("stock is not custom");
            }
        }
        akcija.setOznakaHartije(request.getOznaka().toUpperCase());
        akcija.setOpisHartije(request.getOpis());
        akcija.setBerza(berza);
        akcija.setOutstandingShares(request.getOutstandingShares());
        akcija.setLastUpdated(new Date());
        akcija.setCustom(true);

        return akcijeRepository.save(akcija);
    }

    public FuturesUgovori createUpdateFuturesUgovor(FuturesCreateUpdateRequest request) {
        if(StringUtils.emptyString(request.getOznaka()) ||
                StringUtils.emptyString(request.getOpis()) ||
                StringUtils.emptyString(request.getContractUnit()) ||
                request.getContractSize() == null ||
                request.getMaintenanceMargin() == null ||
                request.getSettlementDate() == null) {
            throw new RuntimeException("bad request");
        }

        Berza berza = null;
        if(!StringUtils.emptyString(request.getBerzaOznaka())) {
            berza = berzaRepository.findBerzaByOznakaBerze(request.getBerzaOznaka());
            if(berza == null) {
                throw new RuntimeException("exchange not found");
            }
        }

        FuturesUgovori futuresUgovor = new FuturesUgovori();
        if(request.getId() != null) {
            Optional<FuturesUgovori> fu = futuresUgovoriRepository.findById(request.getId());
            if(fu.isEmpty()) {
                throw new RuntimeException("futures contract not found");
            }

            futuresUgovor = fu.get();
            if(!futuresUgovor.getCustom()) {
                throw new RuntimeException("futures contract is not custom");
            }
        }
        futuresUgovor.setOznakaHartije(request.getOznaka().toUpperCase());
        futuresUgovor.setOpisHartije(request.getOpis());
        futuresUgovor.setBerza(berza);
        futuresUgovor.setContractSize(request.getContractSize());
        futuresUgovor.setContractUnit(request.getContractUnit());
        futuresUgovor.setMaintenanceMargin(request.getMaintenanceMargin());
        futuresUgovor.setSettlementDate(request.getSettlementDate());
        futuresUgovor.setLastUpdated(new Date());
        futuresUgovor.setCustom(true);

        return futuresUgovoriRepository.save(futuresUgovor);
    }

    public OrderResponse makeOrder(String token, OrderRequest orderRequest) {
        // Korak 1: Odredi tip hartije od vrednosti koji se trguje, odredi da li je buy ili sell akcija, odredi koji je tip ordera
        HartijaOdVrednostiType hartijaTip = HartijaOdVrednostiType.valueOf(orderRequest.getHartijaOdVrednostiTip().toUpperCase()); // AKCIJA, FOREX, FUTURES_UGOVOR
        OrderAction orderAkcija = OrderAction.valueOf(orderRequest.getAkcija().toUpperCase()); // BUY ili SELL
        OrderType orderType = getOrderType(orderRequest.getLimitValue(), orderRequest.getStopValue()); // MARKET, STOP, LIMIT, STOP_LIMIT

        // Korak 1a: Nije definisana akcija SELL za Forex, posto Forex uvek koristi par (prodajem EUR da bih kupio USD)
        if(hartijaTip.equals(HartijaOdVrednostiType.FOREX) && orderAkcija.equals(OrderAction.SELL)) {
            return new OrderResponse(MessageUtils.ORDER_REJECTED);
        }

        // Korak 2: Preuzmi podatke o hartiji o vrednosti
        AskBidPriceDto askBidPrice = priceService.getAskBidPrice(hartijaTip, orderRequest.getSymbol());

        // Korak 2a: Proveri ispravnost hartije od vrednosti
        if(askBidPrice.getHartijaId() == -1L)
            return new OrderResponse(MessageUtils.ERROR);

        // Korak 3: Izracunaj ukupnu cenu i proviziju
        Double ukupnaCena = getPrice(askBidPrice.getAsk(), askBidPrice.getBid(), orderAkcija) * orderRequest.getKolicina();
        Double provizija = getCommission(ukupnaCena, orderType);

        // Korak 4: Odredi order status, tj. da li order mora da bude approvovan ili je automatski approvovan
        String valuta = "USD";
        if(askBidPrice.getBerza() != null) {
            valuta = askBidPrice.getBerza().getValuta().getKodValute();
        }
        OrderStatus status = getOrderStatus(token, ukupnaCena, valuta, orderRequest.isMarginFlag());

        // Korak 4a: Uzmi ID korisnika kako bi mogli da vezemo porudzbinu za korisnika
        Long userId = userService.getUserByToken(token).getId();

        // Korak 4b: Konverzija ordera u USD ako je u pitanju margins order
        if(!valuta.equals("USD") && orderRequest.isMarginFlag()) {
            ForexPodaciDto exchangeRate = forexPodaciService.getForexBySymbol(valuta, "USD");
            ukupnaCena *= exchangeRate.getExchangeRate();
        }

        // Korak 5: Sacuvaj order u bazi podataka
        Order order = orderService.saveOrder(token, orderRequest, userId, askBidPrice.getBerza(), askBidPrice.getHartijaId(), hartijaTip, orderAkcija, ukupnaCena,
                provizija, orderType, status);
        if(order == null) {
            return new OrderResponse(MessageUtils.ERROR);
        }

        // Korak 6: Vrati poruku da je order primljen
        return new OrderResponse(MessageUtils.ORDER_SUCCESSFUL);
    }

    public Double getPrice(Double ask, Double bid, OrderAction orderAction){
        List<Double> cene;
        Random random = new Random();

        Double toReturn;
        if(orderAction.equals(OrderAction.BUY)) {
            // TODO: Prepraviti ovo.
//            cene = transakcijaService.findPriceActionBuy(bid);
//            if(cene.size() >= 3)
//                return cene.get(random.nextInt(3));
            toReturn = bid;
        } else { // SELL
            // TODO: Prepraviti ovo.
//            cene = transakcijaService.findPriceActionBuy(ask);
//            if(cene.size() >= 3)
//                return cene.get(random.nextInt(3));
            toReturn = ask;
        }

        return toReturn;
    }

    public Double getCommission(Double price, OrderType orderType) {
        if(orderType.equals(OrderType.MARKET_ORDER))
            return Math.min(0.14 * price, 7);
        return Math.min(0.24 * price, 12);
    }

    public OrderType getOrderType(Integer limitValue, Integer stopValue) {
        if(limitValue > 0 && stopValue > 0)
            return OrderType.STOP_LIMIT_ORDER;
        else if(limitValue > 0)
            return OrderType.LIMIT_ORDER;
        else if(stopValue > 0)
            return OrderType.STOP_ORDER;
        return OrderType.MARKET_ORDER;
    }

    public OrderStatus getOrderStatus(String token, double price, String valuta, boolean margin) {
        UserRole role = UserRole.valueOf(userService.getUserRoleByToken(token));

        if(role.equals(UserRole.ROLE_AGENT)) {
            if(margin) {
                return OrderStatus.ON_HOLD;
            }

            UserDto user = userService.getUserByToken(token);
            Double presostaoLimit = user.getLimit() - user.getLimitUsed();
            if(!valuta.equals("RSD")) {
                ForexPodaciDto exchangeRate = forexPodaciService.getForexBySymbol(valuta, "RSD");
                price *= exchangeRate.getExchangeRate();
            }
            if(user.isNeedsSupervisorPermission() || (presostaoLimit - price < 0))
                return OrderStatus.ON_HOLD;
        }

        return OrderStatus.APPROVED;
    }

}
