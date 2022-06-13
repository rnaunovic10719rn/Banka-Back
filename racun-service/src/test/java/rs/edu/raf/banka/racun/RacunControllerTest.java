package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.net.HttpHeaders;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.controller.RacunController;

import rs.edu.raf.banka.racun.dto.*;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.RacunService;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(RacunController.class)
public class RacunControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    RacunService racunService;

    @MockBean
    SredstvaKapitalService sredstvaKapitalService;

    @MockBean
    TransakcijaService transakcijaService;

    @Autowired
    private ObjectMapper objectMapper;

    TransakcijaRequest transakcijaRequest = initTransakcijaRequest();

    DateFilter dateFilter = initDateFilter();

    String validJWToken = initValidJWT();
    String invalidJWToken = initInvalidJWT();

    String dummyName = "Mock";
    UUID mockRacun = UUID.randomUUID();
    String mockValuta = "RSD";


    @Test
    void testDodajTransakciju() throws Exception {

        when(transakcijaService.dodajTransakciju(validJWToken, transakcijaRequest)).thenReturn(new Transakcija());

        mockMvc.perform(post("/api/racun/transakcija").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcijaRequest)))
                .andExpect(status().isOk());
    }


    @Test
    void testDodajTransakcijuBadReq() throws Exception {

        transakcijaRequest.setOrderId(null);
        transakcijaRequest.setRezervisano(100);
        when(transakcijaService.dodajTransakciju(validJWToken, transakcijaRequest)).thenReturn(new Transakcija());

        mockMvc.perform(post("/api/racun/transakcija").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcijaRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDodajTransakcijuNull() throws Exception {

        when(transakcijaService.dodajTransakciju(validJWToken, transakcijaRequest)).thenReturn(null);

        mockMvc.perform(post("/api/racun/transakcija").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcijaRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetTransakcijeSaValutom() throws Exception {
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll(validJWToken, mockValuta)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije/{valuta}", mockValuta).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransakcije() throws Exception {
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll(validJWToken)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransakcijeIFilterom() throws Exception {
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll(validJWToken, dateFilter.from, dateFilter.to)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateFilter)))
                .andExpect(status().isOk());
    }




    @Test
    void testGetTransakcijeSaValutomiFilterom() throws Exception {
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll(validJWToken, mockValuta, dateFilter.from, dateFilter.to)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije/{valuta}", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateFilter)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStanjeSupervisor() throws Exception {
        List<SupervisorSredstvaKapitalDto> list = new ArrayList<>();

        when(sredstvaKapitalService.findSredstvaKapitalSupervisor(validJWToken)).thenReturn(list);

        mockMvc.perform(get("/api/racun/stanjeSupervisor", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStanjeHartija() throws Exception {
        UUID mockUUID = UUID.randomUUID();
        when(sredstvaKapitalService.get(any(UUID.class), any(KapitalType.class), anyLong())).thenReturn(new SredstvaKapital());

        mockMvc.perform(get("/api/racun/stanje/{racun}/{hartijaType}/{hartijaId}", String.valueOf(mockUUID),KapitalType.NOVAC.toString(),1L).header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStanjeAgent() throws Exception {
        when(sredstvaKapitalService.findSredstvaKapitalAgent(validJWToken)).thenReturn(new AgentSredstvaKapitalDto());

        mockMvc.perform(get("/api/racun/stanjeAgent").header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStanje() throws Exception {
        List<KapitalHartijeDto> kapitalHartijeDtoList = new ArrayList<>();
        when(sredstvaKapitalService.getUkupnoStanjePoHartijama(validJWToken)).thenReturn(kapitalHartijeDtoList);
        mockMvc.perform(get("/api/racun/kapitalStanje").header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStanjeBadReq() throws Exception {

        when(sredstvaKapitalService.getUkupnoStanjePoHartijama(validJWToken)).thenReturn(null);
        mockMvc.perform(get("/api/racun/kapitalStanje").header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetStanjePoTipu() throws Exception {
        List<KapitalPoTipuHartijeDto> kapitalPoTipuHartijeDtos = new ArrayList<>();
        when(sredstvaKapitalService.getStanjeJednogTipaHartije(anyString(), anyString())).thenReturn(kapitalPoTipuHartijeDtos);

        mockMvc.perform(get("/api/racun/kapitalStanje/{kapitalType}",KapitalType.NOVAC.toString()).header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStanjePoTipuBadRequest() throws Exception {
        when(sredstvaKapitalService.getStanjeJednogTipaHartije(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/api/racun/kapitalStanje/{kapitalType}",KapitalType.NOVAC.toString()).header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTransakcijeHartije() throws Exception {
        List<TransakcijeHartijeDto> transakcijeHartijeDtos = new ArrayList<>();
        when(sredstvaKapitalService.getTransakcijeHartije(anyLong(),anyString())).thenReturn(transakcijeHartijeDtos);

        mockMvc.perform(get("/api/racun/transakcijaHartije/{kapitalType}/{id}",KapitalType.NOVAC.toString(),1L).header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransakcijeHartijeBad() throws Exception {
        when(sredstvaKapitalService.getTransakcijeHartije(anyLong(),anyString())).thenReturn(null);

        mockMvc.perform(get("/api/racun/transakcijaHartije/{kapitalType}/{id}",KapitalType.NOVAC.toString(),1L).header(HttpHeaders.AUTHORIZATION,  validJWToken))
                .andExpect(status().isBadRequest());
    }



    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject(dummyName + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    String initInvalidJWT() {
        return JWT.create()
                .withSubject(dummyName + ",ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"X_LIST_USERS", "DUMMY_FAKE_PERMISSION"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }


    private TransakcijaRequest initTransakcijaRequest() {
        TransakcijaRequest tr = new TransakcijaRequest();
        tr.setBrojRacuna(mockRacun);
        tr.setOpis("mockOpis");
        tr.setValutaOznaka(mockValuta);
        tr.setOrderId(1L);
        tr.setUplata(1000);
        tr.setIsplata(0);
        tr.setRezervisano(0);
        tr.setLastSegment(false);

        return tr;
    }

    private DateFilter initDateFilter() {
        DateFilter df = new DateFilter();
        df.from = new Date();
        df.to = new Date();

        return df;
    }



}
