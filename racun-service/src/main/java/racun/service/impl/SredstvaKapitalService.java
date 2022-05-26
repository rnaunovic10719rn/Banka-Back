package racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import racun.model.SredstvaKapital;
import racun.repository.RacunRepository;
import racun.repository.SredstvaKapitalRepository;

import java.util.List;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final RacunRepository racunRepository;


    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository,RacunRepository racunRepository){
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.racunRepository = racunRepository;
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
            }else{
                sredstvaKapital = new SredstvaKapital();
                sredstvaKapital.setRacun(racunRepository.findByUser(userID));
                sredstvaKapital.setUkupno(uplata-isplata);
                sredstvaKapital.setRezervisano(rezervisano-rezervisanoKoristi);
                sredstvaKapital.setRaspolozivo(0);
                sredstvaKapitalRepository.save(sredstvaKapital);
            }
            return null;
    }
}
