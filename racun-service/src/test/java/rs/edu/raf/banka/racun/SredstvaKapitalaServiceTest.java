package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SredstvaKapitalaServiceTest {

    @InjectMocks
    private SredstvaKapitalService sredstvaKapitalService;

    @Mock
    private RacunRepository racunRepository;

    @Mock
    private ValutaRepository valutaRepository;

    @Mock
    private SredstvaKapitalRepository sredstvaKapitalRepository;

    @Test
    void testPocetnoStanje() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);

        given(racunRepository.findByBrojRacuna(any())).willReturn(r);
        given(valutaRepository.findValutaByKodValute("RSD")).willReturn(new Valuta());
        given(sredstvaKapitalRepository.save(any())).willReturn(sredstvaKapital);

        assertEquals(sredstvaKapitalService.pocetnoStanje(r.getBrojRacuna(),"RSD",1000).getUkupno(),1000);
    }

    @Test
    void testGetAll() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        given(sredstvaKapitalRepository.findByRacunAndValuta(racunRepository.findByBrojRacuna(r.getBrojRacuna()), valutaRepository.findValutaByKodValute("RSD"))).willReturn(sredstvaKapital);

       // assertEquals(sredstvaKapitalService.getAll(r.getBrojRacuna(),"RSD"),sredstvaKapital);
    }


}
