package rs.edu.raf.banka.racun;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.controller.UgovorController;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.model.company.CompanyBankAccount;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;
import rs.edu.raf.banka.racun.model.contract.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaRequest;
import rs.edu.raf.banka.racun.requests.UgovorCreateRequest;
import rs.edu.raf.banka.racun.requests.UgovorUpdateRequest;
import rs.edu.raf.banka.racun.service.impl.UgovorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static rs.edu.raf.banka.racun.RacunControllerTest.asJsonString;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UgovorController.class)
public class UgovorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UgovorService ugovorService;

    @Mock
    MockMultipartFile mockMultipartFile;

    UgovorCreateRequest ugovorCreateRequest = initUgovorCreateRequest();

    UgovorUpdateRequest ugovorUpdateRequest = initUgovorUpdateRequest();

    TransakcionaStavkaRequest transakcionaStavkaRequestUpdate = initTransakcionaStavkaUpdate();

    TransakcionaStavkaRequest transakcionaStavkaRequestCreate = initTransakcionaStavkaCreate();

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

    @Test
    void testCreateUgovor() throws Exception {
        Ugovor ugovor = new Ugovor();
        when(ugovorService.createUgovor(any(), anyString())).thenReturn(ugovor);
        mockMvc.perform(post("/api/ugovor/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ugovorCreateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testEditUgovor() throws Exception {
        Ugovor ugovor = new Ugovor();
        when(ugovorService.modifyUgovor(any(), anyString())).thenReturn(ugovor);
        mockMvc.perform(put("/api/ugovor/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ugovorUpdateRequest)))
                .andExpect(status().isOk());
    }

//    @Test
//    void testFinalizeUgovor() throws Exception {
//        Ugovor ugovor = new Ugovor();
//        when(ugovorService.finalizeUgovor(any(), any(), anyString())).thenReturn(ugovor);
//        mockMvc.perform(fileUpload("/api/ugovor/finalize/{id}", 1L)
//                        .file(mockMultipartFile)
//                        .header(HttpHeaders.AUTHORIZATION, validJWToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(ugovorUpdateRequest)))
//                .andExpect(status().isOk());
//    }

    @Test
    void testRejectUgovor() throws Exception {
        Ugovor ugovor = new Ugovor();
        when(ugovorService.rejectUgovor(any(), anyString())).thenReturn(ugovor);
        mockMvc.perform(fileUpload("/api/ugovor/reject/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ugovorUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDocument() throws Exception {
        Binary data = new Binary("mockData".getBytes());
        when(ugovorService.getContractDocument(anyString())).thenReturn(data);
        mockMvc.perform(get("/api/ugovor//document/{documentId}","mockId").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateStavka() throws Exception {
        TransakcionaStavka transakcionaStavka = new TransakcionaStavka();
        when(ugovorService.addStavka(any(), anyString())).thenReturn(transakcionaStavka);
        mockMvc.perform(post("/api/ugovor/stavka").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcionaStavkaRequestCreate)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStavka() throws Exception {
        TransakcionaStavka transakcionaStavka = new TransakcionaStavka();
        when(ugovorService.getTransakcionaStavkaById(anyLong(), anyString())).thenReturn(transakcionaStavka);
        mockMvc.perform(get("/api/ugovor/id/{id}",1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testEditStavka() throws Exception {
        TransakcionaStavka transakcionaStavka = new TransakcionaStavka();
        when(ugovorService.modifyStavka(any(), anyString())).thenReturn(transakcionaStavka);
        mockMvc.perform(put("/api/ugovor/stavka").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transakcionaStavkaRequestUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteStavka() throws Exception {
        mockMvc.perform(delete("/api/ugovor/stavka/{stavkaId}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    public TransakcionaStavkaRequest initTransakcionaStavkaUpdate() {
        TransakcionaStavkaRequest transakcionaStavkaRequest = new TransakcionaStavkaRequest();
        transakcionaStavkaRequest.setStavkaId(1L);
        transakcionaStavkaRequest.setKapitalTypePotrazni(KapitalType.AKCIJA);
        transakcionaStavkaRequest.setKapitalPotrazniOznaka("mockOznakaP");
        transakcionaStavkaRequest.setKolicinaPotrazna(10.0);
        transakcionaStavkaRequest.setKapitalPotrazniId(1L);
        transakcionaStavkaRequest.setKapitalTypeDugovni(KapitalType.NOVAC);
        transakcionaStavkaRequest.setKapitalDugovniOznaka("mockOznakaD");
        transakcionaStavkaRequest.setKolicinaDugovna(1000.0);
        transakcionaStavkaRequest.setKapitalDugovniId(1L);
        return transakcionaStavkaRequest;
    }

    public TransakcionaStavkaRequest initTransakcionaStavkaCreate() {
        TransakcionaStavkaRequest transakcionaStavkaRequest = new TransakcionaStavkaRequest();
        transakcionaStavkaRequest.setUgovorId(1L);
        transakcionaStavkaRequest.setKapitalTypePotrazni(KapitalType.AKCIJA);
        transakcionaStavkaRequest.setKapitalPotrazniOznaka("mockOznakaP");
        transakcionaStavkaRequest.setKolicinaPotrazna(10.0);
        transakcionaStavkaRequest.setKapitalPotrazniId(1L);
        transakcionaStavkaRequest.setKapitalTypeDugovni(KapitalType.NOVAC);
        transakcionaStavkaRequest.setKapitalDugovniOznaka("mockOznakaD");
        transakcionaStavkaRequest.setKolicinaDugovna(1000.0);
        transakcionaStavkaRequest.setKapitalDugovniId(1L);
        return transakcionaStavkaRequest;
    }

    public UgovorCreateRequest initUgovorCreateRequest() {
        UgovorCreateRequest ugovorCreateRequest = new UgovorCreateRequest();
        ugovorCreateRequest.setCompanyId(1L);
        ugovorCreateRequest.setDelovodniBroj("mockDelovodniBroj");
        ugovorCreateRequest.setDescription("mockDescription");
        return ugovorCreateRequest;
    }

    public UgovorUpdateRequest initUgovorUpdateRequest() {
        UgovorUpdateRequest ugovorUpdateRequest = new UgovorUpdateRequest();
        ugovorUpdateRequest.setCompanyId(1L);
        ugovorUpdateRequest.setDelovodniBroj("mockDelovodniBroj");
        ugovorUpdateRequest.setDescription("mockDescription");
        return ugovorUpdateRequest;
    }

    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject(dummyName + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

}
