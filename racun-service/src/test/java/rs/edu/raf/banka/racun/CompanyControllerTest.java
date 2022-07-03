package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.net.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.edu.raf.banka.racun.controller.CompanyController;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.company.CompanyBankAccount;
import rs.edu.raf.banka.racun.model.company.CompanyContactPerson;
import rs.edu.raf.banka.racun.requests.CompanyBankAccountRequest;
import rs.edu.raf.banka.racun.requests.CompanyContactPersonRequest;
import rs.edu.raf.banka.racun.requests.CompanyRequest;
import rs.edu.raf.banka.racun.service.impl.CompanyBankAccountService;
import rs.edu.raf.banka.racun.service.impl.CompanyContactPersonService;
import rs.edu.raf.banka.racun.service.impl.CompanyService;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static rs.edu.raf.banka.racun.RacunControllerTest.asJsonString;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CompanyController.class)
public class CompanyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CompanyService companyService;

    @MockBean
    CompanyContactPersonService contactPersonService;

    @MockBean
    CompanyBankAccountService bankAccountService;

    CompanyBankAccountRequest  companyBankAccountRequest =  initCompanyBankAccountRequest();
    CompanyRequest companyRequest = initCompanyRequest();
    CompanyContactPersonRequest companyContactPeopleRequest = initCompanyContactPersonRequest();
    String validJWToken = initValidJWT();
    String dummyName = "Mock";

    @Test
    void testGetCompanies() throws Exception {

        ArrayList<Company> companies = new ArrayList<>();
        when(companyService.getCompanies()).thenReturn(companies);
        mockMvc.perform(get("/api/company/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetCompanyById() throws Exception {

        Company company = new Company();
        when(companyService.getCompanyById(1L)).thenReturn(Optional.of(company));

        mockMvc.perform(get("/api/company/id/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCompanyByIdEmpty() throws Exception {

        mockMvc.perform(get("/api/company/id/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCompanyByNaziv() throws Exception {

        ArrayList<Company> companies = new ArrayList<>();
        when(companyService.getCompanyByNaziv("mock")).thenReturn(companies);
        mockMvc.perform(get("/api/company/naziv/{naziv}", "mock").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCompanyByMaticniBroj() throws Exception {

        Company company = new Company();
        when(companyService.getCompanyByMaticniBroj("mock")).thenReturn(company);
        mockMvc.perform(get("/api/company/maticniBroj/{maticniBroj}", "mock").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCompanyByPib() throws Exception {

        Company company = new Company();
        when(companyService.getCompanyByPib("mock")).thenReturn(company);
        mockMvc.perform(get("/api/company/pib/{pib}", "mock").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void testCreateCompany() throws Exception {

        Company company = new Company();
        when(companyService.createCompany(companyRequest)).thenReturn(company);
        mockMvc.perform(post("/api/company/").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(companyRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testEditCompany() throws Exception {

        Company company = new Company();
        when(companyService.editCompany(companyRequest)).thenReturn(company);
        mockMvc.perform(post("/api/company/edit").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(companyRequest)))
                .andExpect(status().isOk());
    }


    @Test
    void testGetContactPersons() throws Exception {
        List<CompanyContactPerson> companyContactPeople = new ArrayList<>();
        when(contactPersonService.getContactPersons(anyLong())).thenReturn(companyContactPeople);
        mockMvc.perform(get("/api/company/contact/{companyId}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetContactPerson() throws Exception {
        CompanyContactPerson companyContactPeople = new CompanyContactPerson();
        when(contactPersonService.getContactPersonById(anyLong())).thenReturn(Optional.of(companyContactPeople));
        mockMvc.perform(get("/api/company/contact/id/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetContactPersonNotFound() throws Exception {
        mockMvc.perform(get("/api/company/contact/id/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateContactPerson() throws Exception {
        CompanyContactPerson editContactPerson = new CompanyContactPerson();
        when(contactPersonService.createContactPerson(any())).thenReturn(editContactPerson);
        mockMvc.perform(post("/api/company/contact").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(companyContactPeopleRequest)))
                .andExpect(status().isOk());
    }


    @Test
    void testEditContactPerson() throws Exception {
        CompanyContactPerson editContactPerson = new CompanyContactPerson();
        when(contactPersonService.editContactPerson(any())).thenReturn(editContactPerson);
        mockMvc.perform(post("/api/company/contact/edit").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(companyContactPeopleRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteContactPerson() throws Exception {
        mockMvc.perform(delete("/api/company/contact/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetBankAccounts() throws Exception {
        List<CompanyBankAccount> bankAccounts = new ArrayList<>();
        when(bankAccountService.getBankAccounts(anyLong())).thenReturn(bankAccounts);
        mockMvc.perform(get("/api/company/bankaccount/{companyId}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBankAccountNotFound() throws Exception {
        mockMvc.perform(get("/api/company//bankaccount/id/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBankAccount() throws Exception {
        CompanyBankAccount companyBankAccount = new CompanyBankAccount();
        when(bankAccountService.getBankAccountById(anyLong())).thenReturn(Optional.of(companyBankAccount));
        mockMvc.perform(get("/api/company/bankaccount/id/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateBankAccount() throws Exception {
        CompanyBankAccount companyBankAccount = new  CompanyBankAccount();
        when(bankAccountService.createBankAccount(any())).thenReturn(companyBankAccount);
        mockMvc.perform(post("/api/company/bankaccount").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(companyBankAccountRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testEditBankAccount() throws Exception {
        CompanyBankAccount companyBankAccount = new  CompanyBankAccount();
        when(bankAccountService.editBankAccount(any())).thenReturn(companyBankAccount);
        mockMvc.perform(post("/api/company/bankaccount/edit").header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(companyContactPeopleRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteBankAccount() throws Exception {
        mockMvc.perform(delete("/api/company/bankaccount/{id}", 1L).header(HttpHeaders.AUTHORIZATION, validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject(dummyName + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    public CompanyRequest initCompanyRequest() {
        CompanyRequest companyRequest = new CompanyRequest();
        companyRequest.setNaziv("mock");
        companyRequest.setMaticniBroj("mock");
        companyRequest.setPib("mockPiB");
        companyRequest.setSifraDelatnosti("mockSifra");
        companyRequest.setAdresa("mockAdresa");
        companyRequest.setDrzava("mockDrzava");
        return companyRequest;
    }

    public CompanyContactPersonRequest initCompanyContactPersonRequest() {
        CompanyContactPersonRequest companyRequest = new CompanyContactPersonRequest();
        companyRequest.setCompanyId(1L);
        companyRequest.setIme("mock");
        companyRequest.setPrezime("mock");
        companyRequest.setEmail("mock");
        companyRequest.setBrojTelefona("mock");
        companyRequest.setPozicija("mock");
        companyRequest.setPozicija("mock");
        companyRequest.setNapomena("mock");
        return companyRequest;
    }

    public CompanyBankAccountRequest initCompanyBankAccountRequest() {
        CompanyBankAccountRequest companyRequest = new CompanyBankAccountRequest();
        companyRequest.setCompanyId(1L);
        companyRequest.setValutaId(1L);
        companyRequest.setBrojRacuna("mockBroj");
        companyRequest.setBanka("mockBanka");
        companyRequest.setActive(true);
        return companyRequest;
    }


}
