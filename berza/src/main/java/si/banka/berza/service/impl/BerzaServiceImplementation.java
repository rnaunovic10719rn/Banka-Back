package si.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.banka.berza.model.Berza;
import si.banka.berza.response.OrderStatusResponse;
import si.banka.berza.service.BerzaServiceRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class BerzaServiceImplementation {

    private BerzaServiceRepository berzaRepository;

    @Autowired
    public BerzaServiceImplementation(BerzaServiceRepository berzaRepository){
        this.berzaRepository = berzaRepository;
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
        return (differenceInMilliSeconds / (60 * 60 * 1000)) % 24 < 4;
    }
}
