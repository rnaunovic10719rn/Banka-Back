package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.model.Transakcija;
import rs.edu.raf.banka.berza.repository.TranskacijaRepository;
import rs.edu.raf.banka.berza.service.impl.TransakcijaService;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransakcijaServiceTest {

    @InjectMocks
    TransakcijaService transakcijaService;

    @Mock
    TranskacijaRepository transkacijaRepository;

    @Test
    void testSaveTransakcija() {
        Transakcija transakcija = new Transakcija();
        transakcija.setId(1L);
        when(transkacijaRepository.save(transakcija)).thenReturn(transakcija);
        assertEquals(transakcija.getId(), transakcijaService.saveTranskacija(transakcija).getId());
    }

    @Test
    void testFindPriceActionBuy() {
        Double bid = 1.0;
        when(transkacijaRepository.findCeneTransakcijaBuy(any(Date.class), eq(bid))).thenReturn(List.of(1.0));
        assertEquals(1.0, transakcijaService.findPriceActionBuy(bid).get(0));
    }

    @Test
    void testFindPriceActionSell() {
        Double ask = 1.0;
        when(transkacijaRepository.findCeneTransakcijaSell(any(Date.class), eq(ask))).thenReturn(List.of(1.0));
        assertEquals(1.0, transakcijaService.findPriceActionSell(ask).get(0));
    }
}
