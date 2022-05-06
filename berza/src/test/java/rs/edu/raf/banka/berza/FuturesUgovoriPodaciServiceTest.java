package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FuturesUgovoriPodaciServiceTest {

    @InjectMocks
    FuturesUgovori futuresUgovori;

    @Mock
    FuturesUgovoriRepository futuresUgovoriRepository;

    @Test
    void test() {

    }
}
