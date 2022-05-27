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

    public SredstvaKapital getAll(String username){
        return sredstvaKapitalRepository.findByUser(username);
    }

    public SredstvaKapital updateStanje(String username,String racun,double uplata, long isplata, double rezervisano,double rezervisanoKoristi){
            SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByUser(username);
            SredstvaKapital sredstvaKapital_primalac = sredstvaKapitalRepository.findByRacun(racun);

            if (!racun.equals("")){ //Provera inicijalnog kreiranja racuna
                sredstvaKapital.setUkupno(sredstvaKapital.getUkupno()+uplata-isplata);
                sredstvaKapital_primalac.setUkupno(sredstvaKapital_primalac.getUkupno()+isplata-uplata);
                sredstvaKapital.setRezervisano(sredstvaKapital.getRezervisano()+rezervisano-rezervisanoKoristi);
                return sredstvaKapitalRepository.save(sredstvaKapital);
            }else{
                sredstvaKapital = new SredstvaKapital();
                sredstvaKapital.setRacun(racunRepository.findByUser(username));
                sredstvaKapital.setUkupno(uplata-isplata);
                sredstvaKapital.setRezervisano(rezervisano-rezervisanoKoristi);
                sredstvaKapital.setRaspolozivo(0);
                sredstvaKapitalRepository.save(sredstvaKapital);
            }
            return null;
    }
}
