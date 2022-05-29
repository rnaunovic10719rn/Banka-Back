package rs.edu.raf.banka.racun.service.impl;

import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.model.SredstvaKapital;

import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

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

    public SredstvaKapital getAll(UUID racun, String valuta) {
        return sredstvaKapitalRepository.findByRacunAndValuta(racunRepository.findByBrojRacuna(racun), valutaRepository.findValutaByOznakaValute(valuta));
    }

    public SredstvaKapital updateStanje(UUID racun, double iznos, double rezervisano, double rezervisanoKoristi, String valuta,long hartijeOdVrednostiID) {
        SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacunAndValuta(racunRepository.findByBrojRacuna(racun), valutaRepository.findValutaByOznakaValute(valuta));

        if (sredstvaKapital != null) { //Provera inicijalnog kreiranja racuna
            if(sredstvaKapital.getUkupno() + iznos<0){
                return null;
            }
            sredstvaKapital.setUkupno(sredstvaKapital.getUkupno() + iznos);
            sredstvaKapital.setRezervisano(sredstvaKapital.getRezervisano() + rezervisano - rezervisanoKoristi);
            sredstvaKapital.setRaspolozivo(sredstvaKapital.getUkupno() - rezervisano);

        } else {
            sredstvaKapital = new SredstvaKapital();
            sredstvaKapital.setRacun(racunRepository.findByBrojRacuna(racun));
            sredstvaKapital.setUkupno(iznos);
            sredstvaKapital.setRezervisano(rezervisano - rezervisanoKoristi);
            sredstvaKapital.setRaspolozivo(sredstvaKapital.getUkupno() - rezervisano);
            sredstvaKapital.setValuta(valutaRepository.findValutaByOznakaValute(valuta)); //inicijana valuta
        }

        sredstvaKapital.setHaritjeOdVrednostiID(hartijeOdVrednostiID);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }
}
