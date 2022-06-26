package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.service.impl.AkcijePodaciService;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;
import rs.edu.raf.banka.berza.service.impl.PriceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceServiceTest {

    @InjectMocks
    PriceService priceService;

    @Mock
    AkcijePodaciService akcijePodaciService;

    @Mock
    ForexPodaciService forexPodaciService;

    @Mock
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Test
    void testGetAskBidPriceAkcije(){
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.AKCIJA;
        String symbol = "symbol";

        AkcijePodaciDto dto = new AkcijePodaciDto();
        dto.setId(1L);
        dto.setBerzaId(1L);
        dto.setPrice(10.0);

        when(akcijePodaciService.getAkcijaByTicker(any())).thenReturn(dto);
        assertEquals(1L, priceService.getAskBidPrice(hartijaOdVrednostiType, symbol).getHartijaId());
    }

    @Test
    void testGetAskBidPriceFutures(){
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FUTURES_UGOVOR;
        String symbol = "symbol";

        FuturesPodaciDto dto = new FuturesPodaciDto();
        dto.setId(1L);
        dto.setHigh(10.0);

        when(futuresUgovoriPodaciService.getFuturesUgovor(any())).thenReturn(dto);
        assertEquals(1L, priceService.getAskBidPrice(hartijaOdVrednostiType, symbol).getHartijaId());
    }

    @Test
    void testGetAskBidPriceForex(){
        HartijaOdVrednostiType hartijaOdVrednostiType = HartijaOdVrednostiType.FOREX;
        String symbol = "symbol symbol";

        ForexPodaciDto dto = new ForexPodaciDto();
        dto.setId(1L);
        dto.setAsk(10.0);
        dto.setBid(10.0);

        when(forexPodaciService.getForexBySymbol(any(), any())).thenReturn(dto);
        assertEquals(1L, priceService.getAskBidPrice(hartijaOdVrednostiType, symbol).getHartijaId());
    }
}
