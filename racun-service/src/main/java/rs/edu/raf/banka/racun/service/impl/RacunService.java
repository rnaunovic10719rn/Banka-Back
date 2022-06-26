package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.util.UUID;


@Service
public class RacunService {

    private RacunRepository racunRepository;
    private ValutaRepository valutaRepository;
    private final SredstvaKapitalService sredstvaKapitalService;

    @Autowired
    public RacunService(RacunRepository racunRepository, SredstvaKapitalService sredstvaKapitalService, ValutaRepository valutaRepository){
        this.racunRepository = racunRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.valutaRepository = valutaRepository;
    }

    public Racun createRacun(){
        Racun racun = new Racun();
        racun.setBrojRacuna(UUID.randomUUID());
        racun.setTipRacuna(RacunType.KES);
        racunRepository.save(racun);
        sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), "RSD", 100000);
        sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), "USD", 100000);
        sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), "EUR", 100000);

        return racun;
    }

}
