package racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import racun.model.SredstvaKapital;
import racun.repository.RacunRepository;
import racun.repository.SredstvaKapitalRepository;

import java.util.List;
import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final RacunRepository racunRepository;


    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository, RacunRepository racunRepository) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.racunRepository = racunRepository;
    }

    public SredstvaKapital getAll(String username) {
        return sredstvaKapitalRepository.findByUser(username);
    }

    public SredstvaKapital updateStanje(String username, UUID racun, double iznos, double rezervisano, double rezervisanoKoristi) {
        System.out.println(username);
        SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByUser(username);

        if (sredstvaKapital != null) { //Provera inicijalnog kreiranja racuna
            SredstvaKapital sredstvaKapitalPrimalac = sredstvaKapitalRepository.findByRacun(racunRepository.findByBrojRacuna(racun));
            sredstvaKapital.setUkupno(sredstvaKapital.getUkupno() + iznos);
            sredstvaKapitalPrimalac.setUkupno(sredstvaKapitalPrimalac.getUkupno() + iznos * (-1));
            sredstvaKapital.setRezervisano(sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi);
            return sredstvaKapitalRepository.save(sredstvaKapital);
        } else {
            sredstvaKapital = new SredstvaKapital();
            sredstvaKapital.setRacun(racunRepository.findByUsername(username));
            sredstvaKapital.setUkupno(iznos);
            sredstvaKapital.setRezervisano(rezervisano - rezervisanoKoristi);
            sredstvaKapital.setRaspolozivo(0);
            return sredstvaKapitalRepository.save(sredstvaKapital);
        }
    }
}
