package racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import racun.model.SredstvaKapital;
import racun.repository.SredstvaKapitalRepository;

@Service
public class SredstvaKapitalService {

    private SredstvaKapitalRepository sredstvaKapitalRepository;


    public SredstvaKapitalService(){

    }

    public SredstvaKapital updateStanje(long userID,double uplata, long isplata, double rezervisano,double rezervisanoKoristi){
            SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByUser(userID);
            if (sredstvaKapital!=null){
                sredstvaKapital.setUkupno(sredstvaKapital.getUkupno()+uplata-isplata);
                sredstvaKapital.setRezervisano(sredstvaKapital.getRezervisano()+rezervisano-rezervisanoKoristi);
                return sredstvaKapitalRepository.save(sredstvaKapital);
            }
            return null;
    }
}
