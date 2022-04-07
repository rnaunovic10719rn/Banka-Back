package si.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
    private TranskacijaRepository transkacijaRepository;

    @Autowired
    public BerzaServiceImplementation(BerzaServiceRepository berzaRepository, UserAccountRepository userAccountRepository,
                                      AkcijeRepository akcijeRepository, ForexRepository forexRepository, FuturesUgovoriRepository futuresUgovoriRepository,
                                      TranskacijaRepository transkacijaRepository){
        this.berzaRepository = berzaRepository;
        this.userAccountRepository = userAccountRepository;
        this.akcijeRepository = akcijeRepository;
        this.forexRepository = forexRepository;
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.transkacijaRepository = transkacijaRepository;
    }


    public MakeOrderResponse makeOrder(Long user_id, Long hartijaId, String hartijaTip, Integer kolicina, String action, List<String> types){
        UserAccount userAccount = userAccountRepository.getById(user_id);
        HartijaOdVrednostiType hartijaType = HartijaOdVrednostiType.valueOf(hartijaTip.toUpperCase());
        OrderAction orderAction = OrderAction.valueOf(action.toUpperCase());
        List<OrderType> orderTypes = new ArrayList<>();
        for(String type : types){
            orderTypes.add(OrderType.valueOf(type.toUpperCase()));
        }

        Double ask = 0.0;
        Double bid = 0.0;
        if(hartijaType.equals(HartijaOdVrednostiType.AKCIJA)){
            Akcije akcije = akcijeRepository.getById(hartijaId);
            ask = akcije.getAsk();
            bid = akcije.getBid();
        }
        else if(hartijaType.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)){
            FuturesUgovori futuresUgovori = futuresUgovoriRepository.getById(hartijaId);
            ask = futuresUgovori.getAsk();
            bid = futuresUgovori.getBid();
        }
        else if(hartijaType.equals(HartijaOdVrednostiType.FOREX)){
            Forex forex = forexRepository.getById(hartijaId);
            ask = forex.getAsk();
            bid = forex.getBid();
        }

        Double ukupnaCena = izracunajCenu(ask, bid, orderAction);
        if(ukupnaCena * kolicina > userAccount.getWallet())
            return new MakeOrderResponse("You don't have enough money for this action.");

        //TODO: nastaviti sa tipovima order-a

        return null;
    }

    private Double izracunajCenu(Double ask, Double bid, OrderAction orderAction){
        List<Double> cene;
        Random random = new Random();

        Double toReturn;
        if(orderAction.equals(OrderAction.BUY)) {
            cene = transkacijaRepository.findCeneTransakcijaBuy(new Date(), bid);
            if(cene.size() >= 3)
                return cene.get(random.nextInt(3));
            toReturn = bid;
        }
        else {
            cene = transkacijaRepository.findCeneTransakcijaSell(new Date(), ask);
            if(cene.size() >= 3)
                return cene.get(random.nextInt(3));
            toReturn = ask;
        }

        return toReturn;
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

    private Object getHartijaByType(HartijaOdVrednostiType hartijaType){
        if(hartijaType.equals(HartijaOdVrednostiType.AKCIJA))
            return new Akcije();
        else if(hartijaType.equals(HartijaOdVrednostiType.FOREX))
            return new Forex();
        else if(hartijaType.equals(HartijaOdVrednostiType.FUTURES_UGOVOR))
            return new FuturesUgovori();
        return null;
    }
}
