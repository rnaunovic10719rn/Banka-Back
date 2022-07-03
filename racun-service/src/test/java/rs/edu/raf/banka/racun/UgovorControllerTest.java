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
import rs.edu.raf.banka.racun.controller.UgovorController;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.service.impl.UgovorService;

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
@WebMvcTest(UgovorController.class)
public class UgovorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UgovorService ugovorService;

    String validJWToken = initValidJWT();
    String dummyName = "Mock";

    @Test
    void testGetUgovor() throws Exception {
        Ugovor u = new Ugovor();
        when(ugovorService.getUgovorById(anyLong(), anyString())).thenReturn(u);
        mockMvc.perform(get("/api/ugovor/id/{id}",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAll() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAll(anyString())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllFinalized() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllFinalized(anyString())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/finalized/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllDraft() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllDraft(anyString())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/draft/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllKompanija() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllByCompany(anyLong(),anyString())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/company/{kompanijaId}/",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllKompanijaFinalized() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllByCompanyAndUgovorStatus(anyLong(),anyString(),any())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/company/{kompanijaId}/finalized",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllKompanijaDraft() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllByCompanyAndUgovorStatus(anyLong(),anyString(),any())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/company/{kompanijaId}/draft",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllBrUgovora() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllByDelovodniBroj(anyString(),anyString())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/delovodnibroj/{delovodniBroj}","mockBroj").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllBrUgovoraFinalized() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllByDelovodniBrojAndUgovorStatus(anyString(),anyString(),any())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/delovodnibroj/{delovodniBroj}/finalized","mockBroj").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllBrUgovoraDraft() throws Exception {
        List<Ugovor> ugovors = new ArrayList<>();
        when(ugovorService.getAllByDelovodniBrojAndUgovorStatus(anyString(),anyString(),any())).thenReturn(ugovors);
        mockMvc.perform(get("/api/ugovor/delovodnibroj/{delovodniBroj}/draft","mockBroj").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject(dummyName + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

}
