package si.banka.berza.service.impl;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import si.banka.berza.enums.HartijaOdVrednostiType;
import si.banka.berza.enums.OrderAction;
import si.banka.berza.enums.OrderType;
import si.banka.berza.model.*;
import si.banka.berza.repository.*;
import si.banka.berza.response.MakeOrderResponse;
import si.banka.berza.response.OrderStatusResponse;
import si.banka.berza.service.BerzaServiceRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BerzaServiceImplementation {

    private BerzaServiceRepository berzaRepository;
    private UserAccountRepository userAccountRepository;
    private AkcijeRepository akcijeRepository;
    private ForexRepository forexRepository;
    private FuturesUgovoriRepository futuresUgovoriRepository;
    private OrderService orderService;
    private TransakcijaService transakcijaService;

    @Autowired
    public BerzaServiceImplementation(BerzaServiceRepository berzaRepository, UserAccountRepository userAccountRepository,
                                      AkcijeRepository akcijeRepository, ForexRepository forexRepository, FuturesUgovoriRepository futuresUgovoriRepository,
                                      TransakcijaService transakcijaService, OrderService orderService){
        this.berzaRepository = berzaRepository;
        this.userAccountRepository = userAccountRepository;
        this.akcijeRepository = akcijeRepository;
        this.forexRepository = forexRepository;
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.transakcijaService = transakcijaService;
        this.orderService = orderService;
    }


    public void addOrderToBerza(Order order, Long berzaId){
        Berza berza = berzaRepository.findById_berze(berzaId);
        berza.getOrderi().add(order);
        berzaRepository.save(berza);
    }

    public MakeOrderResponse makeOrder(Long berzaId, Long userId, Long hartijaId, String hartijaTipString, Integer kolicina, String action,
                                       Integer limitValue, Integer stopValue, boolean isAON, boolean isMargin){

        UserAccount userAccount = userAccountRepository.getById(userId);
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
        if(hartijaTip.equals(HartijaOdVrednostiType.AKCIJA)){
            Akcije akcije = akcijeRepository.getById(hartijaId);
            ask = akcije.getAsk();
            bid = akcije.getBid();
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)){
            FuturesUgovori futuresUgovori = futuresUgovoriRepository.getById(hartijaId);
            ask = futuresUgovori.getAsk();
            bid = futuresUgovori.getBid();
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FOREX)){
            Forex forex = forexRepository.getById(hartijaId);
            ask = forex.getAsk();
            bid = forex.getBid();
        }

        Double ukupnaCena = getPrice(ask, bid, orderAkcija);
      
//        if(ukupnaCena * kolicina > userAccount.getWallet())
//            return new MakeOrderResponse("You don't have enough money for this action.");

//        if(ukupnaCena * kolicina > userAccount.getWallet())
//            return new MakeOrderResponse("You don't have enough money for this action.");

        Double provizija = getCommission(ukupnaCena, orderType);

        Order order = orderService.saveOrder(userAccount, hartijaId, hartijaTip, kolicina, orderAkcija, ukupnaCena,
                provizija, orderType, isAON, isMargin);
        executeTransaction(berzaId, order, ask, bid);

        return new MakeOrderResponse("Order Successful");
    }

    private MakeOrderResponse executeTransaction(Long berzaId, Order order, Double ask, Double bid){
        OrderStatusResponse orderStatus = this.getOrderStatus(berzaId);
        boolean flag = orderStatus.isBerzaOtvorena();
        if(order.isAON()){
            Transakcija transakcija;
            if(flag)
                transakcija = transactionOrder(order.getKolicina(), order, ask, bid);
            else
                transakcija = transactionOrderWithDelay(order.getKolicina(), order, ask, bid);
            this.addOrderToBerza(order, berzaId);
            return new MakeOrderResponse("OK");
        }

        return executeMiniTransactions(berzaId, order, flag, ask, bid);
    }

    /**
     * Margin je povezan sa walletom korisnika koji ce biti detaljnije objasnjen u drugoj iteraciji
     * s obzirom na to, bice obradjen nakon nastavka specifikacije
     */
    private MakeOrderResponse executeMiniTransactions(Long berzaId, Order order, boolean flag, Double ask, Double bid){
        Random random = new Random();
        int kolicina = order.getKolicina();
        int kolicinaZaTransakciju = random.nextInt(kolicina) + 1;

        if(order.getOrderAction().equals(OrderAction.BUY) && !canExecuteTransactionBuy(order, bid))
            return new MakeOrderResponse("You can't proceed this action.");

        if(order.getOrderAction().equals(OrderAction.SELL) && !canExecuteTransactionBuy(order, ask))
            return new MakeOrderResponse("You can't proceed this action.");

        while(kolicina - kolicinaZaTransakciju >= 0){
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
    private Transakcija transactionOrderWithDelay(Integer transactionAmount, Order order, Double ask, Double bid){
        try {
            //3s simulaciju 30 minuta
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return transactionOrder(transactionAmount, order, ask, bid);
    }

    private boolean canExecuteTransactionBuy(Order order, Double bid){
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

    private boolean canExecuteTransactionSell(Order order, Double ask){
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

    private Double getPrice(Double ask, Double bid, OrderAction orderAction){
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

    private Double getCommission(Double price, OrderType orderType) {
        if(orderType.equals(OrderType.MARKET_ORDER))
            return Math.min(0.14 * price, 7);
        return Math.min(0.24 * price, 12);
    }

    public OrderStatusResponse getOrderStatus(Long id){
        Berza berza = berzaRepository.findById_berze(id);
        Date date = new Date();

        //format 9:30 a.m. to 4:00 p.m.
        String pre_market = berza.getPre_market_radno_vreme();
        pre_market = pre_market.replace("a.m.", "AM");
        pre_market = pre_market.replace("p.m.", "PM");
        String post_market = berza.getPost_market_radno_vreme();
        post_market = post_market.replace("a.m.", "AM");
        post_market = post_market.replace("p.m.", "PM");

        String preSplit[] = pre_market.split("to");
        String postSplit[] = post_market.split("to");

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        try {
            if(isOverlapping(dateFormat.parse(preSplit[0]), dateFormat.parse(preSplit[1]), date) ||
                    isOverlapping(dateFormat.parse(postSplit[0]), dateFormat.parse(postSplit[1]), date))
                return new OrderStatusResponse(true, "Order odobren.");

            if(differenceInHours(dateFormat.parse(postSplit[1]), date) ||
                    differenceInHours(dateFormat.parse(preSplit[1]), date))
                return new OrderStatusResponse(false, "Berza je trenutno u after-hours stanju.");

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new OrderStatusResponse(false, "Berza ne radi.");
    }

    private boolean isOverlapping(Date start, Date end, Date timeToCheck){
        return start.before(timeToCheck) && end.after(timeToCheck);
    }

    private boolean differenceInHours(Date closingTime, Date timeToCheck){
        long differenceInMilliSeconds = timeToCheck.getTime() - closingTime.getTime();
        return (differenceInMilliSeconds / (60 * 60 * 1000)) % 24 <= 4;
    }

}
