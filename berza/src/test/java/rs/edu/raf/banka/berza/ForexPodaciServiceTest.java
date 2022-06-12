package rs.edu.raf.banka.berza;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.request.ForexTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.Valuta;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.ValutaRepository;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;
import rs.edu.raf.banka.berza.utils.DateUtils;

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
    void testGetForexTimeseriesIntraDay5minSATURDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay5minSUNDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay5minMONDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDa30minSATURDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay30minSUNDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay30minMONDAY() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay30minDefault() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay60minDefault1m() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay60minDefault6m() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("6m");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay60minDefault1y() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("1y");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay60minDefault2y() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("2y");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

    @Test
    void testGetForexTimeseriesIntraDay60minDefaultytd() {
        ForexTimeseriesUpdateRequest readReq = new ForexTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("ytd");
        try (MockedStatic<DateUtils> utilities = Mockito.mockStatic(DateUtils.class)) {
            utilities.when(DateUtils::getZonedDateTime).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
            utilities.when(() -> DateUtils.getZonedDateTime(any())).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        }
        assertNotNull(forexPodaciService.getForexTimeseries(readReq));
    }

}
