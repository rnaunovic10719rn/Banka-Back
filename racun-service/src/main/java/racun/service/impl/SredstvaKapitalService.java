package racun.service.impl;

import org.springframework.stereotype.Service;
import racun.model.SredstvaKapital;
import racun.repository.RacunRepository;
import racun.repository.SredstvaKapitalRepository;
import racun.repository.ValutaRepository;

import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final RacunRepository racunRepository;
    private final ValutaRepository valutaRepository;


    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository, RacunRepository racunRepository, ValutaRepository valutaRepository) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
    }

    public SredstvaKapital getAll(UUID racun) {
        return sredstvaKapitalRepository.findByRacun(racunRepository.findByBrojRacuna(racun));
    }

    public SredstvaKapital updateStanje(UUID racun, double iznos, double rezervisano, double rezervisanoKoristi) {
        SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacun(racunRepository.findByBrojRacuna(racun));

        if (sredstvaKapital != null) { //Provera inicijalnog kreiranja racuna
            sredstvaKapital.setUkupno(sredstvaKapital.getUkupno() + iznos);
            sredstvaKapital.setRezervisano(sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi);
            sredstvaKapital.setRaspolozivo(sredstvaKapital.getUkupno()-rezervisano);
            return sredstvaKapitalRepository.save(sredstvaKapital);
        } else {
            sredstvaKapital = new SredstvaKapital();
            sredstvaKapital.setRacun(racunRepository.findByBrojRacuna(racun));
            sredstvaKapital.setUkupno(iznos);
            sredstvaKapital.setRezervisano(rezervisano - rezervisanoKoristi);
            sredstvaKapital.setRaspolozivo(sredstvaKapital.getUkupno()-rezervisano);
            sredstvaKapital.setValuta(valutaRepository.findValutaByOznakaValute("RSD")); //inicijana valuta
            return sredstvaKapitalRepository.save(sredstvaKapital);
        }
    }
}
