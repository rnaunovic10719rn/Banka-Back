package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.util.UUID;


@Service
public class RacunService {

    private RacunRepository racunRepository;
    private final SredstvaKapitalService sredstvaKapitalService;

    @Autowired
    public RacunService(RacunRepository racunRepository, SredstvaKapitalService sredstvaKapitalService){
        this.racunRepository = racunRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
    }

    public Racun createKesRacun(){
        Racun racun = new Racun();
        racun.setBrojRacuna(UUID.randomUUID());
        racun.setTipRacuna(RacunType.KES);
        racunRepository.save(racun);
        sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), "RSD", 100000);
        sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), "USD", 100000);
        sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), "EUR", 100000);

        return racun;
    }

    public Racun createMarginRacun(){
        Racun racun = new Racun();
        racun.setBrojRacuna(UUID.randomUUID());
        racun.setTipRacuna(RacunType.MARGINS_RACUN);
        racunRepository.save(racun);
        sredstvaKapitalService.pocetnoStanjeMarzniRacun(racun.getBrojRacuna(), KapitalType.NOVAC, -1L);

        return racun;
    }

}
