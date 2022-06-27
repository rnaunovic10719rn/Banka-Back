package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.model.HartijaOdVrednosti;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.repository.HartijaRepository;
import rs.edu.raf.banka.berza.service.impl.HartijaService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HartijaSeviceTest {

    @InjectMocks
    HartijaService hartijaService;

    @Mock
    AkcijeRepository akcijeRepository;

    @Mock
    ForexRepository forexRepository;

    @Mock
    FuturesUgovoriRepository futuresUgovoriRepository;

    @Mock
    HartijaRepository hartijaRepository;

    @Test
    void testGetAllNearSettlement(){
        Akcije a = new Akcije();
        a.setOpisHartije("opis");
        List<HartijaOdVrednosti> list = new ArrayList<>();
        list.add(a);
        when(hartijaRepository.getAllNearSettlement()).thenReturn(list);
        assertEquals("opis",hartijaService.getAllNearSettlement().get(0).getOpisHartije());
    }

    @Test
    void testFindHartijaByIdAndTypeAkcije(){
        String hartijaType = "AKCIJA";
        Akcije akcije = new Akcije();
        akcije.setOpisHartije("opis");

        when(akcijeRepository.findAkcijeById(any())).thenReturn(akcije);
        assertEquals("opis", hartijaService.findHartijaByIdAndType(1L, hartijaType).getOpisHartije());
    }

    @Test
    void testFindHartijaByIdAndTypeForex(){
        String hartijaType = "FOREX";
        Forex forex = new Forex();
        forex.setOpisHartije("opis");

        when(forexRepository.findForexById(any())).thenReturn(forex);
        assertEquals("opis", hartijaService.findHartijaByIdAndType(1L, hartijaType).getOpisHartije());
    }

    @Test
    void testFindHartijaByIdAndTypeFuture(){
        String hartijaType = "FUTURE_UGOVOR";
        FuturesUgovori fu = new FuturesUgovori();
        fu.setOpisHartije("opis");

        when(futuresUgovoriRepository.findFuturesById(any())).thenReturn(fu);
        assertEquals("opis", hartijaService.findHartijaByIdAndType(1L, hartijaType).getOpisHartije());
    }

    @Test
    void testFindHartijaByIdAndTypeError(){
        String hartijaType = "error";
        assertThrows(ArrayIndexOutOfBoundsException.class, ()-> hartijaService.findHartijaByIdAndType(1L, hartijaType));
    }
}
