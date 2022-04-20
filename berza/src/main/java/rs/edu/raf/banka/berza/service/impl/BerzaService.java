package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderType;
import rs.edu.raf.banka.berza.model.*;
import rs.edu.raf.banka.berza.repository.*;
import rs.edu.raf.banka.berza.response.MakeOrderResponse;
import rs.edu.raf.banka.berza.response.OrderStatusResponse;
import rs.edu.raf.banka.berza.repository.BerzaRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BerzaService {

    private BerzaRepository berzaRepository;
    private UserAccountRepository userAccountRepository;
    private AkcijeRepository akcijeRepository;
    private ForexRepository forexRepository;
    private FuturesUgovoriRepository futuresUgovoriRepository;
    private OrderService orderService;
    private TransakcijaService transakcijaService;
    private AkcijePodaciService akcijePodaciService;
    private ForexPodaciService forexPodaciService;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Autowired
    public BerzaService(BerzaRepository berzaRepository, UserAccountRepository userAccountRepository,
                        AkcijeRepository akcijeRepository, ForexRepository forexRepository, FuturesUgovoriRepository futuresUgovoriRepository,
                        TransakcijaService transakcijaService, OrderService orderService, AkcijePodaciService akcijePodaciService,
                        ForexPodaciService forexPodaciService, FuturesUgovoriPodaciService futuresUgovoriPodaciService){
        this.berzaRepository = berzaRepository;
        this.userAccountRepository = userAccountRepository;
        this.akcijeRepository = akcijeRepository;
        this.forexRepository = forexRepository;
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.transakcijaService = transakcijaService;
        this.orderService = orderService;
        this.akcijePodaciService = akcijePodaciService;
        this.forexPodaciService = forexPodaciService;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
    }

    public List<Berza> findAll(){
        return berzaRepository.findAll();
    }

    public void addOrderToBerza(Order order, Long berzaId){
        Berza berza = berzaRepository.findBerzaById(berzaId);
        berza.getOrderi().add(order);
        berzaRepository.save(berza);
    }

    public Berza findBerza(String oznaka){
        return berzaRepository.findBerzaByOznakaBerze(oznaka);
    }

    public Akcije findAkcije(String symbol){
        return akcijeRepository.findAkcijeByOznakaHartije(symbol);
    }

    public MakeOrderResponse makeOrder(Long userId, String symbol, String hartijaTipString, Integer kolicina, String action,
                                       Integer limitValue, Integer stopValue, boolean isAON, boolean isMargin){
//        UserAccount userAccount = userAccountRepository.getById(userId);
        HartijaOdVrednostiType hartijaTip = HartijaOdVrednostiType.valueOf(hartijaTipString.toUpperCase());
        OrderAction orderAkcija = OrderAction.valueOf(action.toUpperCase());
        OrderType orderType = OrderType.MARKET_ORDER;

        if(limitValue > 0 && stopValue > 0)
            orderType = OrderType.STOP_LIMIT_ORDER;
        else if(limitValue > 0)
            orderType = OrderType.LIMIT_ORDER;
        else if(stopValue > 0)
            orderType = OrderType.STOP_ORDER;

        Double ask = 0.0;
        Double bid = 0.0;
        Long hartijaId = -1L;
        Long berzaId = -1L;
        if(hartijaTip.equals(HartijaOdVrednostiType.AKCIJA)){
            AkcijePodaciDto akcije = akcijePodaciService.getAkcijaByTicker(symbol);
  //          Akcije akcije = akcijeRepository.findAkcijeByOznakaHartije(symbol);
            if(akcije != null) {
                hartijaId = akcije.getId();
                berzaId = akcije.getBerzaId();
                //ne postoje podaci o asku, bidu, uzima se trnt cena
                ask = akcije.getPrice();
                bid = akcije.getPrice();
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)){
            FuturesPodaciDto futuresUgovori = futuresUgovoriPodaciService.getFuturesUgovor(symbol);
//            FuturesUgovori futuresUgovori = futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(symbol);
            if(futuresUgovori != null) {
                hartijaId = futuresUgovori.getId();
//                berzaId = futuresUgovori.getBerza().getId();
                ask = futuresUgovori.getHigh();
                bid = futuresUgovori.getHigh();
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FOREX)){
            String split[] = symbol.split(" ");
            ForexPodaciDto forex = forexPodaciService.getForexBySymbol(split[0], split[1]);
//            Forex forex = forexRepository.findForexByOznakaHartije(symbol);
            if(forex != null) {
                hartijaId = forex.getId();
//                berzaId = forex.getBerza().getId();
                ask = forex.getAsk();
                bid = forex.getBid();
            }
        }

        if(hartijaId == -1L)
            return new MakeOrderResponse("Error");

        Double ukupnaCena = getPrice(ask, bid, orderAkcija);
      
//        if(ukupnaCena * kolicina > userAccount.getWallet())
//            return new MakeOrderResponse("You don't have enough money for this action.");

//        if(ukupnaCena * kolicina > userAccount.getWallet())
//            return new MakeOrderResponse("You don't have enough money for this action.");

        Double provizija = getCommission(ukupnaCena, orderType);

        Order order = orderService.saveOrder(userId, hartijaId, hartijaTip, kolicina, orderAkcija, ukupnaCena,
                provizija, orderType, isAON, isMargin);
        executeTransaction(berzaId, order, ask, bid);

        return new MakeOrderResponse("Order Successful");
    }

    public MakeOrderResponse executeTransaction(Long berzaId, Order order, Double ask, Double bid){
        boolean flag = true;
        if(berzaId != -1){
            OrderStatusResponse orderStatus = this.getOrderStatus(berzaId);
            flag = orderStatus.isBerzaOtvorena();
        }
        if(order.isAON()){
            Transakcija transakcija;
            if(flag)
                transakcija = transactionOrder(order.getKolicina(), order, ask, bid);
            else
                transakcija = transactionOrderWithDelay(order.getKolicina(), order, ask, bid);
            if(berzaId != -1)
                this.addOrderToBerza(order, berzaId);
            return new MakeOrderResponse("OK");
        }

        return executeMiniTransactions(berzaId, order, flag, ask, bid);
    }

    /**
     * Margin je povezan sa walletom korisnika koji ce biti detaljnije objasnjen u drugoj iteraciji
     * s obzirom na to, bice obradjen nakon nastavka specifikacije
     */
    public MakeOrderResponse executeMiniTransactions(Long berzaId, Order order, boolean flag, Double ask, Double bid){
        Random random = new Random();
        int kolicina = order.getKolicina();
        int kolicinaZaTransakciju = random.nextInt(kolicina) + 1;

        if(order.getOrderAction().equals(OrderAction.BUY) && !canExecuteTransactionBuy(order, bid))
            return new MakeOrderResponse("You can't proceed this action.");

        if(order.getOrderAction().equals(OrderAction.SELL) && !canExecuteTransactionSell(order, ask))
            return new MakeOrderResponse("You can't proceed this action.");

        while(kolicina - kolicinaZaTransakciju > 0){
            //transakcija fixe delay
            if(flag)
                transactionOrder(kolicinaZaTransakciju, order, ask, bid);
            else
                transactionOrderWithDelay(kolicinaZaTransakciju, order, ask, bid);

            kolicina -= kolicinaZaTransakciju;
            kolicinaZaTransakciju = random.nextInt(kolicina) + 1;
        }

        return new MakeOrderResponse("OK");
    }

    @Async
    Transakcija transactionOrder(Integer transactionAmount, Order order, Double ask, Double bid){
        /**
         * MARKET_ORDER se izvrsava odmah, pa nema potrebe da cekamo
         */
        if(!order.getOrderType().equals(OrderType.MARKET_ORDER)) {
            try {
                int s = new Random().nextInt(((int)(0.24*60/order.getKolicina() * 1000)));
                Thread.sleep(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Transakcija transakcija = new Transakcija();
        transakcija.setOrder(order);
        transakcija.setCena(transactionAmount * order.getUkupnaCena());
        transakcija.setKolicina(transactionAmount);
        transakcija.setVremeTranskacije(new Date());
        return transakcijaService.saveTranskacija(transakcija);
    }

    /**
     * ukoliko je berza zatvorena prilikom ordera ili je u after-hours, korisnik ceka duze
     */
    public Transakcija transactionOrderWithDelay(Integer transactionAmount, Order order, Double ask, Double bid){
        try {
            //3s simulaciju 30 minuta
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return transactionOrder(transactionAmount, order, ask, bid);
    }

    public boolean canExecuteTransactionBuy(Order order, Double bid){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getUkupnaCena() <= order.getLimitValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getUkupnaCena() <= order.getLimitValue() && order.getUkupnaCena() < bid){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
                break;
            case STOP_ORDER:
                if(order.getUkupnaCena() < bid)
                    return true;
                break;
            default:
                return true;
        }
        return false;
    }

    public boolean canExecuteTransactionSell(Order order, Double ask){
        switch(order.getOrderType()){
            case LIMIT_ORDER:
                if(order.getUkupnaCena() > order.getLimitValue())
                    return true;
                break;
            case STOP_LIMIT_ORDER:
                if(order.getUkupnaCena() > order.getLimitValue() && order.getUkupnaCena() > ask){
                    order.setOrderType(OrderType.LIMIT_ORDER);
                    return true;
                }
            case STOP_ORDER:
                if(order.getUkupnaCena() > ask)
                    return true;
                break;
            default:
                return true;
        }
        return false;
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

    public OrderStatusResponse getOrderStatus(Long id){
        Berza berza = berzaRepository.findBerzaById(id);
        Date date = new Date();

        String openTime = berza.getOpenTime();
        String closeTime = berza.getCloseTime();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        try {
            if(isOverlapping(dateFormat.parse(openTime), dateFormat.parse(closeTime), date))
                return new OrderStatusResponse(true, "Order odobren.");

            if(differenceInHours(dateFormat.parse(closeTime), date))
                return new OrderStatusResponse(false, "Berza je trenutno u after-hours stanju.");

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new OrderStatusResponse(false, "Berza ne radi.");
    }

    private boolean isOverlapping(Date start, Date end, Date timeToCheck){
        return start.before(timeToCheck) && end.after(timeToCheck);
    }

    public boolean differenceInHours(Date closingTime, Date timeToCheck){
        long differenceInMilliSeconds = timeToCheck.getTime() - closingTime.getTime();
        return (differenceInMilliSeconds / (60 * 60 * 1000)) % 24 <= 4;
    }

}
