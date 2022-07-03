package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.edu.raf.banka.racun.controller.MarginRacunController;
import rs.edu.raf.banka.racun.dto.KapitalHartijeDto;
import rs.edu.raf.banka.racun.dto.KapitalPoTipuHartijeDto;
import rs.edu.raf.banka.racun.dto.MarginTransakcijeHartijeDto;
import rs.edu.raf.banka.racun.dto.SupervisorSredstvaKapitalDto;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.requests.MarginTransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.MarginTransakcijaService;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static rs.edu.raf.banka.racun.RacunControllerTest.asJsonString;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(MarginRacunController.class)
public class MarginRacunControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MarginTransakcijaService marginTransakcijaService;

    @MockBean
    SredstvaKapitalService sredstvaKapitalService;

    MarginTransakcijaRequest marginTransakcijaRequest = initMarginTransakcijaRequest();
    String validJWToken = initValidJWT();
    String dummyName = "Mock";

    @Test
    void testDodajTransakciju() throws Exception {
        MarginTransakcija marginTransakcija = new MarginTransakcija();
        when(marginTransakcijaService.dodajTransakciju(anyString(), any())).thenReturn(marginTransakcija);
        mockMvc.perform(post("/api/margin/transakcija").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(marginTransakcijaRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void testGetTransakcije() throws Exception {
        List<MarginTransakcija> marginTransakcija = new ArrayList<>();
        when(marginTransakcijaService.getAll(anyString())).thenReturn(marginTransakcija);
        mockMvc.perform(get("/api/margin/transakcije").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }


    @Test
    void testGetSredstvaStanje() throws Exception {
        List<SupervisorSredstvaKapitalDto> supervisorSredstvaKapitalDtoList = new ArrayList<>();
        when(sredstvaKapitalService.findSredstvaKapitalSupervisor(anyString(), anyBoolean())).thenReturn(supervisorSredstvaKapitalDtoList);
        mockMvc.perform(get("/api/margin/stanje").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetKapitalStanje() throws Exception {
        List<KapitalHartijeDto> kapitalHartijeDtoList = new ArrayList<>();
        when(sredstvaKapitalService.getUkupnoStanjePoHartijama(anyString(), anyBoolean())).thenReturn(kapitalHartijeDtoList);
        mockMvc.perform(get("/api/margin/kapitalStanje").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetKapitalStanjeBad() throws Exception {

        when(sredstvaKapitalService.getUkupnoStanjePoHartijama(anyString(), anyBoolean())).thenReturn(null);
        mockMvc.perform(get("/api/margin/kapitalStanje").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testGetStanjePoTipu() throws Exception {
        List<KapitalPoTipuHartijeDto> kapitalPoTipuHartijeDtos = new ArrayList<>();
        when(sredstvaKapitalService.getStanjeJednogTipaHartije(anyString(),anyString(), anyBoolean())).thenReturn(kapitalPoTipuHartijeDtos);
        mockMvc.perform(get("/api/margin/kapitalStanje/{kapitalType}","type").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetStanjePoTipuBad() throws Exception {
        when(sredstvaKapitalService.getStanjeJednogTipaHartije(anyString(),anyString(), anyBoolean())).thenReturn(null);
        mockMvc.perform(get("/api/margin/kapitalStanje/{kapitalType}","type").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testGetTransakcijeHartije() throws Exception {
        List<MarginTransakcijeHartijeDto> marginTransakcijeHartijeDtos = new ArrayList<>();
        when(sredstvaKapitalService.getTransakcijeHartijeMargins(anyLong(),anyString())).thenReturn(marginTransakcijeHartijeDtos);
        mockMvc.perform(get("/api/margin/transakcijaHartije/{kapitalType}/{id}","type",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetTransakcijeHartijeBad() throws Exception {
        when(sredstvaKapitalService.getTransakcijeHartijeMargins(anyLong(),anyString())).thenReturn(null);
        mockMvc.perform(get("/api/margin/transakcijaHartije/{kapitalType}/{id}","type",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject(dummyName + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }


    public MarginTransakcijaRequest initMarginTransakcijaRequest(){
        MarginTransakcijaRequest marginTransakcijaRequest = new MarginTransakcijaRequest();
        marginTransakcijaRequest.setOpis("mockOpis");
        marginTransakcijaRequest.setIznos(100L);
        marginTransakcijaRequest.setKredit(100L);
        marginTransakcijaRequest.setMaintenanceMargin(100L);
        marginTransakcijaRequest.setTipKapitala(KapitalType.MARGIN);
        marginTransakcijaRequest.setHartijaId(1L);
        marginTransakcijaRequest.setValutaOznaka("mockValuta");
        marginTransakcijaRequest.setKolicina((double) 100L);
        marginTransakcijaRequest.setUnitPrice((double) 100L);
        marginTransakcijaRequest.setUsername("mockUsername");
        return marginTransakcijaRequest;
    }

}
