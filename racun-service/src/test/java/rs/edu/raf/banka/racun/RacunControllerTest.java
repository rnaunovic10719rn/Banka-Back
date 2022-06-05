package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.net.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.edu.raf.banka.racun.controller.RacunController;
import rs.edu.raf.banka.racun.model.DateFilter;
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
    void testGetStanjeRacuna() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");
        when(sredstvaKapitalService.getAll(mockRacun, mockValuta)).thenReturn(new SredstvaKapital());


        mockMvc.perform(get("/api/racun/stanje/{racun}/{valuta}", mockRacun, mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDodavanjeTransakcije() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");


        when(transakcijaService.dodajTransakciju("Bearer " + validJWToken, transakcijaRequest.getBrojRacuna(), transakcijaRequest.getOpis(), transakcijaRequest.getValutaOznaka(), transakcijaRequest.getOrderId(), transakcijaRequest.getUplata(), transakcijaRequest.getIsplata(), transakcijaRequest.getRezervisano(), transakcijaRequest.getRezervisanoKoristi(), transakcijaRequest.getLastSegment())).thenReturn(new Transakcija());

        mockMvc.perform(post("/api/racun/transakcija").header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcijaRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testDodavanjeTransakcijeNull() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");


        when(transakcijaService.dodajTransakciju("Bearer " + validJWToken, transakcijaRequest.getBrojRacuna(), transakcijaRequest.getOpis(), transakcijaRequest.getValutaOznaka(), transakcijaRequest.getOrderId(), transakcijaRequest.getUplata(), transakcijaRequest.getIsplata(), transakcijaRequest.getRezervisano(), transakcijaRequest.getRezervisanoKoristi(), transakcijaRequest.getLastSegment())).thenReturn(null);

        mockMvc.perform(post("/api/racun/transakcija").header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcijaRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTransakcijeSaValutom() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll("mockUsername", mockValuta)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije/{valuta}", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransakcijeSaValutomiFilterom() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll("mockUsername", mockValuta, dateFilter.from, dateFilter.to)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije/{valuta}", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateFilter)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransakcije() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll("mockUsername")).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransakcijeIFilterom() throws Exception {
        when(userService.getUserByToken(validJWToken)).thenReturn("mockUsername");
        List<Transakcija> list = new ArrayList<>();

        when(transakcijaService.getAll("mockUsername", dateFilter.from, dateFilter.to)).thenReturn(list);

        mockMvc.perform(get("/api/racun/transakcije", mockValuta).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateFilter)))
                .andExpect(status().isOk());
    }


    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    String initValidJWT() {
        return JWT.create()
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
        tr.setRezervisanoKoristi(0);
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
