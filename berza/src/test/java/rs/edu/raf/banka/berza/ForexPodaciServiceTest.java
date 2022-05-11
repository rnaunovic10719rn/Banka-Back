package rs.edu.raf.banka.berza;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.ForexTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.ForexTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.Valuta;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.ValutaRepository;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ForexPodaciServiceTest {

    @Spy
    @InjectMocks
    ForexPodaciService forexPodaciService;

    @Mock
    ForexRepository forexRepository;

    @Mock
    InfluxScrapperService influxScrapperService;

    @Mock
    ValutaRepository valutaRepository;

    @Test
    void testGetAllForex() {
        Forex forex = new Forex();
        forex.setOpisHartije("opisHartije");
        when(forexRepository.findAll()).thenReturn(List.of(forex));
        assertEquals("opisHartije", forexPodaciService.getAllForex().get(0).getOpisHartije());
    }

    @Test
    void testGetOdabraniParovi() {
        ForexPodaciDto dto = new ForexPodaciDto();
        List<ForexPodaciDto> dtoList = new ArrayList<>();
        dtoList.add(dto);
        Valuta valuta = new Valuta();
        Forex forex = new Forex();
        forex.setId(1L);
        when(forexRepository.findForexByBaseCurrencyAndQuoteCurrency(any(), any())).thenReturn(forex);
        when(valutaRepository.findByOznakaValute(any())).thenReturn(valuta);
        when(influxScrapperService.getForexQuote(any(), any())).thenReturn(dtoList);
        assertEquals(1L, forexPodaciService.getOdabraniParovi().get(0).getId());
    }

    @Test
    void testGetOdabraniParoviDtoNull() {
        Valuta valuta = new Valuta();
        Forex forex = new Forex();
        forex.setId(1L);
        when(forexRepository.findForexByBaseCurrencyAndQuoteCurrency(any(), any())).thenReturn(forex);
        when(valutaRepository.findByOznakaValute(any())).thenReturn(valuta);
        when(influxScrapperService.getForexQuote(any(), any())).thenReturn(null);
        assertNull(forexPodaciService.getOdabraniParovi().get(0));
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay5minSATURDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay5minSUNDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-08 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay5minMONDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-09 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa30minSATURDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay30minSUNDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-08 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay30minMONDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-09 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay30minDefault() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay60minDefault1m() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay60minDefault6m() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("6m");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay60minDefault1y() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("1y");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay60minDefault2y() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("2y");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay60minDefaultytd() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("ytd");
        when(forexPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(forexPodaciService.getForexTimeseries(readReq) instanceof List<ForexTimeseriesDto>);
    }

}
