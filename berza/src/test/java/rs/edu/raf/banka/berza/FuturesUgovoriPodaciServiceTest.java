package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;
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
public class FuturesUgovoriPodaciServiceTest {

    @Spy
    @InjectMocks
    FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Mock
    FuturesUgovoriRepository futuresUgovoriRepository;

    @Mock
    InfluxScrapperService influxScrapperService;

    @Test
    void testGetOdabraniFuturesUgovori() {
        FuturesUgovori future = new FuturesUgovori();
        future.setId(1L);
        FuturesPodaciDto dto = new FuturesPodaciDto();
        List<FuturesPodaciDto> res = new ArrayList<>();
        res.add(dto);
        when(futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(any())).thenReturn(future);
        when(influxScrapperService.getFuturesQoute(any())).thenReturn(res);
        assertEquals(1L, futuresUgovoriPodaciService.getOdabraniFuturesUgovori().get(0).getId());
    }


    @Test
    void testGetOdabraniFuturesUgovoriResNull() {
        FuturesUgovori future = new FuturesUgovori();
        future.setId(1L);
        when(futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(any())).thenReturn(future);
        when(influxScrapperService.getFuturesQoute(any())).thenReturn(null);
        assertNull(futuresUgovoriPodaciService.getOdabraniFuturesUgovori().get(0));
    }

    @Test
    void testGetFuturesTimeseriesDay1dSATURDAY() {
        String type = "1d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay1dSUNDAY() {
        String type = "1d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-08 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay1dMONDAY() {
        String type = "1d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-09 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay5dSATURDAY() {
        String type = "5d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-07 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay5dSUNDAY() {
        String type = "5d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-08 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay5dMONDAY() {
        String type = "5d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-09 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay5dDefault() {
        String type = "5d";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay3dDefault1m() {
        String type = "1m";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay3dDefault6m() {
        String type = "6m";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay3dDefault1y() {
        String type = "1y";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay3dDefault2y() {
        String type = "2y";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

    @Test
    void testGetFuturesTimeseriesDay3dDefaultytd() {
        String type = "ytd";
        String symbol = "CONFH2022";
        when(futuresUgovoriPodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertNotNull(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }
}
