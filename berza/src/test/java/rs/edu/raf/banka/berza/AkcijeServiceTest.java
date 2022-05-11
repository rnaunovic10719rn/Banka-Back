package rs.edu.raf.banka.berza;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverviewResponse;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesReadRequest;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.service.impl.AkcijePodaciService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AkcijeServiceTest {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    @InjectMocks
    AkcijePodaciService akcijePodaciService;

    @Mock
    AkcijeRepository akcijeRepository;

    @Mock
    BerzaRepository berzaRepository;

    @Mock
    WebClient influxApiClient;

//    @Test
//    void testGetOdabraneAkcije() {
//        Berza berza = new Berza();
//        when(berzaRepository.findBerzaByOznakaBerze(any())).thenReturn(berza);
//        //when(AlphaVantage.api()).thenReturn(cor);
//        assertEquals(5, akcijePodaciService.getOdabraneAkcije().size());
//    }

//    @Test
//    void testGetAkcijeTimeseries() {
//        AkcijeTimeseriesDto dto = new AkcijeTimeseriesDto();
//        AkcijeTimeseriesReadRequest readReq = new AkcijeTimeseriesReadRequest();
//        WebClient.RequestBodyUriSpec body = null;
//        when(influxApiClient.post().uri("/alphavantage/stock/updateread/")).thenReturn(body).thenReturn(null);
//        assertEquals(true, akcijePodaciService.getAkcijeTimeseries(AkcijeTimeseriesUpdateRequest.getForType("1d","symbol")));
//    }

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
