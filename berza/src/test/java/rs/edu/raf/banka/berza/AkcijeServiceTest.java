package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.service.impl.AkcijePodaciService;
import rs.edu.raf.banka.berza.service.remote.AlphaVantageService;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AkcijeServiceTest {

    @Spy
    @InjectMocks
    AkcijePodaciService akcijePodaciService;

    @Mock
    AkcijeRepository akcijeRepository;

    @Mock
    InfluxScrapperService influxScrapperService;

    @Mock
    AlphaVantageService alphaVantageService;

    @Test
    void testGetOdabraneAkcijeNull() {
        when(alphaVantageService.getCompanyOverview("AAPL")).thenReturn(null);
        assertEquals(5, akcijePodaciService.getOdabraneAkcije().size());
    }

    @Test
    void testGetOdabraneAkcijeAAPL() {
        Berza berza = new Berza();
        berza.setId(1L);
        String ticker = "AAPL" ;
        Akcije akcija = new Akcije();
        akcija.setBerza(berza);
        akcija.setId(1L);
        akcija.setOpisHartije(ticker);
        akcija.setOutstandingShares(10L);
        akcija.setLastUpdated(new Date());
        AkcijePodaciDto dto = new AkcijePodaciDto();
        List<AkcijePodaciDto> dtoList = new ArrayList<>();
        dtoList.add(dto);
        when(akcijeRepository.findAkcijeByOznakaHartije(ticker)).thenReturn(akcija);
        when(influxScrapperService.getStocksQuote(any())).thenReturn(dtoList);
        assertEquals(5, akcijePodaciService.getOdabraneAkcije().size());
    }

    @Test
    void testGetOdabraneAkcijeAAPLDTOnull() {
        Berza berza = new Berza();
        berza.setId(1L);
        String ticker = "AAPL" ;
        Akcije akcija = new Akcije();
        akcija.setBerza(berza);
        akcija.setId(1L);
        akcija.setOpisHartije(ticker);
        akcija.setOutstandingShares(10L);
        akcija.setLastUpdated(new Date());
        when(akcijeRepository.findAkcijeByOznakaHartije(ticker)).thenReturn(akcija);
        when(influxScrapperService.getStocksQuote(any())).thenReturn(null);
        assertEquals(5, akcijePodaciService.getOdabraneAkcije().size());
    }

    @Test
    void testGetOdabraneAkcijeAAPLBerzaNull() {
        Berza berza = new Berza();
        berza.setId(1L);
        String ticker = "AAPL" ;
        Akcije akcija = new Akcije();
        akcija.setBerza(null);
        akcija.setId(1L);
        akcija.setOpisHartije(ticker);
        akcija.setOutstandingShares(10L);
        akcija.setLastUpdated(new Date());
        AkcijePodaciDto dto = new AkcijePodaciDto();
        List<AkcijePodaciDto> dtoList = new ArrayList<>();
        dtoList.add(dto);
        when(akcijeRepository.findAkcijeByOznakaHartije(ticker)).thenReturn(akcija);
        when(influxScrapperService.getStocksQuote(any())).thenReturn(dtoList);
        assertEquals(5, akcijePodaciService.getOdabraneAkcije().size());
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay5minSATURDAY() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa5minSUNDAY() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-08 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa5minMONDAY() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-09 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa5minDefault() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("5min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDay30minSATURDAY() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-07 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa30minSUNDAY() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-08 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa30minMONDAY() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-09 15:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa30minDefault() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("30min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa60minDefault1m() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("1m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa60minDefault6m() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("6m");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa60minDefault1y() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("1y");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa60minDefault2y() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("2y");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }

    @Test
    void testGetAkcijeTimeseriesIntraDa60minDefaultytd() {
        AkcijeTimeseriesUpdateRequest readReq = new AkcijeTimeseriesUpdateRequest();
        readReq.setInterval("60min");
        readReq.setType("intraday");
        readReq.setRequestType("ytd");
        when(akcijePodaciService.getZonedDateTime()).thenReturn(ZonedDateTime.parse("2022-May-10 23:35:05", DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
        assertTrue(akcijePodaciService.getAkcijeTimeseries(readReq) instanceof List<AkcijeTimeseriesDto>);
    }


    @Test
    void testGetById(){
        Akcije akcije = new Akcije();
        akcije.setOpisHartije("MojOpis");
        Long id = 1L;
        when(akcijeRepository.findAkcijeById(id)).thenReturn(akcije);
        assertEquals("MojOpis", akcijePodaciService.getByID(id).getOpisHartije());
    }

    @Test
    void testFilter() {
        String berzaPrefix = "prefix";
        Double priceLowBound = 1.0;
        Double priceUpperBound= 1.0;
        Double askLowBound= 1.0;
        Double askUpperBound= 1.0;
        Double bidLowBound= 1.0;
        Double bidUpperBound= 1.0;
        Long volumeLowBound = 1L;
        Long volumeUpperBound = 1L;
        Integer page = 1;
        Integer size = 1;

        Akcije akcija = new Akcije();
        akcija.setOpisHartije("opisHartije");

        when(akcijeRepository.filterAkcije(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound)).thenReturn(List.of(akcija));
        assertEquals("opisHartije", akcijePodaciService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page, size).getContent().get(0).getOpisHartije());
    }

    @Test
    void testSearch() {
        String oznakaHartije = "oznaka";
        String opisHartije = "opis";
        int page = 1;
        int size = 1;
        Akcije akcija = new Akcije();
        akcija.setOpisHartije(opisHartije);
        akcija.setOznakaHartije(oznakaHartije);
        Page<Akcije> strana = new PageImpl<>(List.of(akcija));

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznakaHartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opisHartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Akcije> example = Example.of(akcija, exampleMatcher);

        when(akcijeRepository.findAll(example, PageRequest.of(page, size))).thenReturn(strana);
        assertEquals(akcija.getOpisHartije(), akcijePodaciService.search(oznakaHartije,opisHartije,page,size).getContent().get(0).getOpisHartije());
    }

}
