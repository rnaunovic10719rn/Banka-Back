package racun.service.impl;

import org.springframework.stereotype.Service;
import racun.model.SredstvaKapital;
import racun.repository.SredstvaKapitalRepository;

import java.util.List;

@Service
public class SredstvaKapitalService {

    private SredstvaKapitalRepository sredstvaKapitalRepository;


    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository){
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
    }

    public SredstvaKapital getAll(long userID){
        return sredstvaKapitalRepository.findByUser(userID);
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
