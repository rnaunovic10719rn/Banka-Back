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
import rs.edu.raf.banka.racun.service.impl.RacunService;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class RacunServiceTest {

    @InjectMocks
    private RacunService racunService;

    @Mock
    private RacunRepository racunRepository;

    @Mock
    private ValutaRepository valutaRepository;


    @Mock
    SredstvaKapitalService sredstvaKapitalService;

    @Mock
    SredstvaKapitalRepository sredstvaKapitalRepository;

    @Test
    void testCreateRacun() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());
        given(racunRepository.save(any())).willReturn(r);
        given(sredstvaKapitalService.pocetnoStanje(any(), anyString(), anyDouble())).willReturn(any());

        assertNotEquals(racunService.createKesRacun(),null);
    }

    @Test
    void testCreateMarginRacun() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());

        given(racunRepository.save(any())).willReturn(r);
        assertNotEquals(racunService.createMarginRacun(),null);
    }
}
