package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.racun.model.MarzniRacun;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.MarzniRacunRepository;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.util.UUID;

@Service
public class MarzniRacunService {

    private MarzniRacunRepository marzniRacunRepository;
    private RacunRepository racunRepository;
    private ValutaRepository valutaRepository;

    @Autowired
    public MarzniRacunService(MarzniRacunRepository marzniRacunRepository, RacunRepository racunRepository, ValutaRepository valutaRepository){
        this.marzniRacunRepository = marzniRacunRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
    }

    public MarzniRacun initMarzniRacun(UUID uuidRacuna, String kodValute, Long hartijaId, HartijaOdVrednostiType hartijaOdVrednostiType){
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }
        Valuta valuta = valutaRepository.findValutaByKodValute(kodValute);
        if(valuta == null) {
            return null;
        }

        MarzniRacun mRacun = new MarzniRacun();
        mRacun.setRacun(racun);
        mRacun.setValuta(valuta);
        mRacun.setHartijaId(hartijaId);
        mRacun.setHartijaOdVrednostiType(hartijaOdVrednostiType);
        mRacun.setUlozenaSredstva(0);
        mRacun.setPozajmljenaSredstva(0);
        mRacun.setMaintenanceMargin(0);
        //Mozda ne treba da se setuje na false uopste jer smo stavili u modelu default false anotaciju
        mRacun.setMarginCall(false);
        return marzniRacunRepository.save(mRacun);
    }
}
