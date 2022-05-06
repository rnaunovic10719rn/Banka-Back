package rs.edu.raf.banka.berza;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ForexPodaciServiceTest {

    @InjectMocks
    ForexPodaciService forexPodaciService;

    @Mock
    ForexRepository forexRepository;

    @Test
    void testGetAllForex() {
        Forex forex = new Forex();
        forex.setOpis_hartije("opisHartije");
        when(forexRepository.findAll()).thenReturn(List.of(forex));
        assertEquals("opisHartije", forexPodaciService.getAllForex().get(0).getOpis_hartije());
    }
}
