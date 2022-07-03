package rs.edu.raf.banka.racun;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.exceptions.ContractExpcetion;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;
import rs.edu.raf.banka.racun.model.contract.TransakcionaStavka;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.repository.contract.TransakcionaStavkaRepository;
import rs.edu.raf.banka.racun.repository.contract.UgovorRepository;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaRequest;
import rs.edu.raf.banka.racun.requests.UgovorCreateRequest;
import rs.edu.raf.banka.racun.requests.UgovorUpdateRequest;
import rs.edu.raf.banka.racun.response.AskBidPriceResponse;
import rs.edu.raf.banka.racun.service.impl.ContractDocumentService;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UgovorService;
import rs.edu.raf.banka.racun.service.impl.UserService;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UgovorServiceTest {

    @InjectMocks
    UgovorService ugovorService;

    @Mock
    UgovorRepository ugovorRepository;

    @Mock
    UserService userService;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    ContractDocumentService contractDocumentService;

    @Mock
    TransakcijaService transakcijaService;

    @Mock
    TransakcionaStavkaRepository transakcionaStavkaRepository;


    @Mock
    ValutaRepository valutaRepository;

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_GL_ADMIN", "ROLE_ADMIN", "ROLE_SUPERVISOR"})
    void testGetByIdSupervisorSameId(String userRole) {
        Long ugovorId = 1L;
        Long ugovorUserId = 1L;
        Long userId = 1L;

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(ugovorUserId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getUgovorById(ugovorId, token), ugovor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_GL_ADMIN", "ROLE_ADMIN", "ROLE_SUPERVISOR"})
    void testGetByIdSupervisorDiffId(String userRole) {
        Long ugovorId = 1L;
        Long ugovorUserId = 2L;
        Long userId = 1L;

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(ugovorUserId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getUgovorById(ugovorId, token), ugovor);
    }

    @Test
    void testGetByIdUserAgentSameId() {
        Long ugovorId = 1L;
        Long ugovorUserId = 1L;
        Long userId = 1L;
        String userRole = "ROLE_AGENT";

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(ugovorUserId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getUgovorById(ugovorId, token), ugovor);
    }

    @Test
    void testGetByIdUserAgentDiffId() {
        Long ugovorId = 1L;
        Long ugovorUserId = 2L;
        Long userId = 1L;
        String userRole = "ROLE_AGENT";

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(ugovorUserId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));
        given(userService.getUserByToken(token)).willReturn(user);

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token), "No permissions");
    }

    @Test
    void testGetByIdUserOtherSameId() {
        Long ugovorId = 1L;
        Long ugovorUserId = 1L;
        Long userId = 1L;
        String userRole = "";

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(ugovorUserId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));
        given(userService.getUserByToken(token)).willReturn(user);

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token), "No permissions");
    }

    @Test
    void testGetByIdUserOtherDiffId() {
        Long ugovorId = 1L;
        Long ugovorUserId = 2L;
        Long userId = 1L;
        String userRole = "";

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(ugovorUserId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));
        given(userService.getUserByToken(token)).willReturn(user);

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token), "No permissions");
    }

    @Test
    void testGetByIdNoUgovor() {
        Long ugovorId = 1L;

        String token = "test";

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.empty());

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token), "Ugovor not found");
    }

    @Test
    void testGetStavkaById() {
        Long ugovorId = 1L;
        Long userId = 1L;
        Long stavkaId = 1L;

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setUserId(userId);

        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");


        var stavka = new TransakcionaStavka();
        stavka.setId(stavkaId);
        stavka.setUgovor(ugovor);

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);
        when(transakcionaStavkaRepository.findById(stavkaId)).thenReturn(Optional.of(stavka));

        assertEquals(ugovorService.getTransakcionaStavkaById(stavkaId, token), stavka);
    }

    @Test
    void testGetStavkaByIdNoStavka() {
        Long stavkaId = 1L;

        when(transakcionaStavkaRepository.findById(stavkaId)).thenReturn(Optional.empty());

        String token = "test";

        assertThrows(ContractExpcetion.class, () -> ugovorService.getTransakcionaStavkaById(stavkaId, token), "Transakciona stavka not found");
    }


    @Test
    void testGetStavkaByIdNoUgovor() {
        Long stavkaId = 1L;

        var stavka = new TransakcionaStavka();
        stavka.setId(stavkaId);

        String token = "test";

        when(transakcionaStavkaRepository.findById(stavkaId)).thenReturn(Optional.of(stavka));

        assertThrows(ContractExpcetion.class, () -> ugovorService.getTransakcionaStavkaById(stavkaId, token), "Ugovor not found");
    }




    @ParameterizedTest
    @ValueSource(strings = {"ROLE_GL_ADMIN", "ROLE_ADMIN", "ROLE_SUPERVISOR"})
    void testGetAllSupervisor(String userRole) {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        given(ugovorRepository.findAll()).willReturn(ugovori);
        given(ugovorRepository.findAllByStatus(UgovorStatus.DRAFT)).willReturn(ugovoriDraft);
        given(ugovorRepository.findAllByStatus(UgovorStatus.FINALIZED)).willReturn(ugovoriFinalized);
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAll(token), ugovori);
        assertEquals(ugovorService.getAllDraft(token), ugovoriDraft);
        assertEquals(ugovorService.getAllFinalized(token), ugovoriFinalized);
    }

    @Test
    void testGetAllAgent() {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_AGENT");

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        given(ugovorRepository.findAllByUserId(userId)).willReturn(ugovori);
        given(ugovorRepository.findAllByStatusAndUserId(UgovorStatus.DRAFT, userId)).willReturn(ugovoriDraft);
        given(ugovorRepository.findAllByStatusAndUserId(UgovorStatus.FINALIZED, userId)).willReturn(ugovoriFinalized);
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAll(token), ugovori);
        assertEquals(ugovorService.getAllDraft(token), ugovoriDraft);
        assertEquals(ugovorService.getAllFinalized(token), ugovoriFinalized);
    }

    @Test
    void testGetAllNoPermission() {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("");

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAll(token), new ArrayList<>());
        assertEquals(ugovorService.getAllDraft(token), new ArrayList<>());
        assertEquals(ugovorService.getAllFinalized(token), new ArrayList<>());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_GL_ADMIN", "ROLE_ADMIN", "ROLE_SUPERVISOR"})
    void testGetAllByCompanySupervisor(String userRole) {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);


        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(ugovorRepository.findAllByCompany(company)).willReturn(ugovori);
        given(ugovorRepository.findAllByCompanyAndStatus(company, UgovorStatus.DRAFT)).willReturn(ugovoriDraft);
        given(ugovorRepository.findAllByCompanyAndStatus(company, UgovorStatus.FINALIZED)).willReturn(ugovoriFinalized);
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAllByCompany(companyId, token), ugovori);
        assertEquals(ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.DRAFT), ugovoriDraft);
        assertEquals(ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.FINALIZED), ugovoriFinalized);
    }

    @Test
    void testGetAllByCompanyAgent() {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_AGENT");

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);


        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(ugovorRepository.findAllByCompanyAndUserId(company, userId)).willReturn(ugovori);
        given(ugovorRepository.findAllByCompanyAndStatusAndUserId(company, UgovorStatus.DRAFT, userId)).willReturn(ugovoriDraft);
        given(ugovorRepository.findAllByCompanyAndStatusAndUserId(company, UgovorStatus.FINALIZED, userId)).willReturn(ugovoriFinalized);
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAllByCompany(companyId, token), ugovori);
        assertEquals(ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.DRAFT), ugovoriDraft);
        assertEquals(ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.FINALIZED), ugovoriFinalized);
    }

    @Test
    void testGetAllByCompanyNoPermission() {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("");

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);


        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAllByCompany(companyId, token), new ArrayList<>());
        assertEquals(ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.DRAFT), new ArrayList<>());
        assertEquals(ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.FINALIZED), new ArrayList<>());
    }

    @Test
    void testGetAllByNoCompany()
    {
        String token = "test";
        Long companyId = 1L;

        given(companyRepository.findById(companyId)).willReturn(Optional.empty());

        assertThrows(ContractExpcetion.class , () -> ugovorService.getAllByCompany(companyId, token), "Company not found");
        assertThrows(ContractExpcetion.class , () -> ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.DRAFT), "Company not found");
        assertThrows(ContractExpcetion.class , () -> ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.FINALIZED), "Company not found");
    }


    @ParameterizedTest
    @ValueSource(strings = {"ROLE_GL_ADMIN", "ROLE_ADMIN", "ROLE_SUPERVISOR"})
    void testGetAllByDelovodniBrojSupervisor(String userRole) {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName(userRole);

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.DRAFT);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        String delovodniBroj = "123-456";

        given(ugovorRepository.findAllByDelovodniBroj(delovodniBroj)).willReturn(ugovori);
        given(ugovorRepository.findAllByDelovodniBrojAndStatus(delovodniBroj, UgovorStatus.DRAFT)).willReturn(ugovoriDraft);
        given(ugovorRepository.findAllByDelovodniBrojAndStatus(delovodniBroj, UgovorStatus.FINALIZED)).willReturn(ugovoriFinalized);
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAllByDelovodniBroj(delovodniBroj, token), ugovori);
        assertEquals(ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.DRAFT), ugovoriDraft);
        assertEquals(ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.FINALIZED), ugovoriFinalized);
    }

    @Test
    void testGetAllByDelovodniBrojAgent() {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_AGENT");

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        String delovodniBroj = "123-456";

        given(ugovorRepository.findAllByDelovodniBrojAndUserId(delovodniBroj, userId)).willReturn(ugovori);
        given(ugovorRepository.findAllByDelovodniBrojAndStatusAndUserId(delovodniBroj, UgovorStatus.DRAFT, userId)).willReturn(ugovoriDraft);
        given(ugovorRepository.findAllByDelovodniBrojAndStatusAndUserId(delovodniBroj, UgovorStatus.FINALIZED, userId)).willReturn(ugovoriFinalized);
        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAllByDelovodniBroj(delovodniBroj, token), ugovori);
        assertEquals(ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.DRAFT), ugovoriDraft);
        assertEquals(ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.FINALIZED), ugovoriFinalized);
    }

    @Test
    void testGetAllByDelovodniBrojNoPermission() {
        List<Ugovor> ugovori = new ArrayList<>();
        List<Ugovor> ugovoriDraft = new ArrayList<>();
        List<Ugovor> ugovoriFinalized = new ArrayList<>();

        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_AGENT");

        var ugovor1 = new Ugovor();
        ugovor1.setUserId(userId);
        ugovor1.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor1);
        ugovoriFinalized.add(ugovor1);

        var ugovor2 = new Ugovor();
        ugovor2.setUserId(userId);
        ugovor2.setStatus(UgovorStatus.FINALIZED);
        ugovori.add(ugovor2);
        ugovoriDraft.add(ugovor2);

        String token = "test";

        String delovodniBroj = "123-456";

        given(userService.getUserByToken(token)).willReturn(user);

        assertEquals(ugovorService.getAllByDelovodniBroj(delovodniBroj, token), new ArrayList<>());
        assertEquals(ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.DRAFT), new ArrayList<>());
        assertEquals(ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.FINALIZED), new ArrayList<>());
    }

    @Test
    void createUgovorTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);


        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));

        var request = new UgovorCreateRequest();
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        when(ugovorRepository.save(any())).thenReturn(new Ugovor());

        assertNotNull(ugovorService.createUgovor(request, token));
    }

    @Test
    void createUgovorBadRequestTest1()
    {
        var request = new UgovorCreateRequest();
        request.setCompanyId(null);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");
        var token = "test";

        assertThrows(ContractExpcetion.class, () -> ugovorService.createUgovor(request, token), "bad request");
    }

    @Test
    void createUgovorBadRequestTest2()
    {
        Long companyId = 1L;
        var request = new UgovorCreateRequest();
        request.setCompanyId(companyId);
        request.setDescription(null);
        request.setDelovodniBroj("123-456");
        var token = "test";

        assertThrows(ContractExpcetion.class, () -> ugovorService.createUgovor(request, token), "bad request");
    }

    @Test
    void createUgovorBadRequestTest3()
    {
        Long companyId = 1L;
        var request = new UgovorCreateRequest();
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj(null);
        var token = "test";

        assertThrows(ContractExpcetion.class, () -> ugovorService.createUgovor(request, token), "bad request");
    }

    @Test
    void createUgovorNoCompanyTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;

        given(companyRepository.findById(companyId)).willReturn(Optional.empty());

        var request = new UgovorCreateRequest();
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        assertThrows(ContractExpcetion.class, () -> ugovorService.createUgovor(request, token), "Company not found");
    }


    @Test
    void modifyUgovorTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));

        var ugovorId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        when(ugovorRepository.save(any())).thenReturn(new Ugovor());

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(new Ugovor()));

        assertNotNull(ugovorService.modifyUgovor(request, token));
    }

    @ParameterizedTest
    @EnumSource(value = UgovorStatus.class, names = {"FINALIZED", "REJECTED"})
    void modifyUgovorFinalizedTest(UgovorStatus status)
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);

        Long companyId = 1L;
        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setStatus(status);

        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(ugovor));

        assertThrows(ContractExpcetion.class, () -> ugovorService.modifyUgovor(request, token), "Ugovor is finalized");
    }

    @Test
    void modifyUgovorNoUgovorTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";


        Long companyId = 1L;

        var ugovorId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.empty());

        assertThrows(ContractExpcetion.class, ()-> ugovorService.modifyUgovor(request, token), "Ugovor not found");
    }

    @Test
    void modifyUgovorNoCompanyTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);

        given(companyRepository.findById(companyId)).willReturn(Optional.empty());


        var ugovorId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(new Ugovor()));

        assertThrows(ContractExpcetion.class, ()-> ugovorService.modifyUgovor(request, token), "Company not found");
    }

    @Test
    void modifyUgovorBadRequest1Test()
    {
        var ugovorId = 1L;
        var companyId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(null);
        request.setDescription(null);
        request.setDelovodniBroj(null);

        var token = "test";

        assertThrows(ContractExpcetion.class, ()-> ugovorService.modifyUgovor(request, token), "bad request");
    }

    @Test
    void modifyUgovorBadRequest2Test()
    {
        var companyId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(null);
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        var token = "test";

        assertThrows(ContractExpcetion.class, ()-> ugovorService.modifyUgovor(request, token), "bad request");
    }

    @Test
    void modifyUgovorBadRequestNoMod1Test()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;

        var ugovorId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(null);
        request.setDescription("Test");
        request.setDelovodniBroj("123-456");

        when(ugovorRepository.save(any())).thenReturn(new Ugovor());

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(new Ugovor()));

        assertNotNull(ugovorService.modifyUgovor(request, token));
    }

    @Test
    void modifyUgovorBadRequestNoMod2Test()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));

        var ugovorId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(companyId);
        request.setDescription(null);
        request.setDelovodniBroj("123-456");

        when(ugovorRepository.save(any())).thenReturn(new Ugovor());

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(new Ugovor()));

        assertNotNull(ugovorService.modifyUgovor(request, token));
    }

    @Test
    void modifyUgovorBadRequestNoMod3Test()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));

        var ugovorId = 1L;
        var request = new UgovorUpdateRequest();
        request.setId(ugovorId);
        request.setCompanyId(companyId);
        request.setDescription("Test");
        request.setDelovodniBroj(null);

        when(ugovorRepository.save(any())).thenReturn(new Ugovor());

        given(ugovorRepository.findById(ugovorId)).willReturn(Optional.of(new Ugovor()));

        assertNotNull(ugovorService.modifyUgovor(request, token));
    }


    @Test
    void finalizeUgovorTest() throws IOException {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        Long ugovorId = 1L;
        var documentId = "id";
        var document = new MockMultipartFile("test", new byte[] {1, 2, 3});

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);

        var stavke = new ArrayList<TransakcionaStavka>();
        var stavka = new TransakcionaStavka();
        stavka.setUserId(userId);
        stavka.setKapitalTypePotrazni(KapitalType.NOVAC);
        stavka.setKolicinaPotrazna(0.1);
        stavka.setKapitalPotrazniId(1L);

        stavka.setKapitalTypeDugovni(KapitalType.AKCIJA);
        stavka.setKolicinaDugovna(0.1);
        stavka.setKapitalDugovniId(1L);

        stavka.setUgovor(new Ugovor());

        stavke.add(stavka);

        ugovor.setStavke(stavke);

        String token = "test";
        given(userService.getUserByToken(token)).willReturn(user);

        when(ugovorRepository.save(ugovor)).thenReturn(ugovor);

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.of(ugovor));


        when(contractDocumentService.saveDocument(ugovor, document)).thenReturn(documentId);

        when(transakcijaService.dodajTransakciju(eq(token), any())).thenReturn(new Transakcija());

        var valuta = new Valuta();
        valuta.setKodValute("USD");
        when(valutaRepository.getById(1L)).thenReturn(valuta);

        assertEquals(ugovorService.finalizeUgovor(ugovorId, document, token), ugovor);
        assertEquals(ugovor.getStatus(), UgovorStatus.FINALIZED);
        assertEquals(ugovor.getDocumentId(), documentId);
    }

    @ParameterizedTest
    @EnumSource(value = UgovorStatus.class, names = {"FINALIZED", "REJECTED"})
    void finalizeUgovorFinalizedTest(UgovorStatus status) throws IOException {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        Long ugovorId = 1L;
        var document = new MockMultipartFile("test", new byte[] {1, 2, 3});

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setStatus(status);

        String token = "test";
        given(userService.getUserByToken(token)).willReturn(user);

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.of(ugovor));

        assertThrows(ContractExpcetion.class, () -> ugovorService.finalizeUgovor(ugovorId, document, token), "Ugovor is finalized");

    }

    @Test
    void finalizeUgovorNoPermissionsTest() throws IOException {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("");

        Long ugovorId = 1L;
        var document = new MockMultipartFile("test", new byte[] {1, 2, 3});

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);

        String token = "test";
        given(userService.getUserByToken(token)).willReturn(user);

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.of(ugovor));

        assertThrows(ContractExpcetion.class, () -> ugovorService.finalizeUgovor(ugovorId, document, token), "No permissions");

    }

    @Test
    void finalizeUgovorBadRequestTest() throws IOException {
        Long ugovorId = 1L;

        MockMultipartFile document = null;
        String token = "test";

        assertThrows(ContractExpcetion.class, () -> ugovorService.finalizeUgovor(ugovorId, document, token), "bad request");
    }

    @Test
    void finalizeUgovorNoUgovor() throws IOException {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        Long ugovorId = 1L;
        var document = new MockMultipartFile("test", new byte[] {1, 2, 3});


        String token = "test";

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.empty());

        assertThrows(ContractExpcetion.class, () -> ugovorService.finalizeUgovor(ugovorId, document, token), "Ugovor not found");
    }

    @Test
    void rejectUgovorTest() throws IOException {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        Long ugovorId = 1L;

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        var stavke = new ArrayList<TransakcionaStavka>();
        var stavka = new TransakcionaStavka();
        stavka.setUserId(userId);
        stavka.setKapitalTypePotrazni(KapitalType.NOVAC);
        stavka.setKolicinaPotrazna(0.1);
        stavka.setKapitalPotrazniId(1L);

        stavka.setKapitalTypeDugovni(KapitalType.AKCIJA);
        stavka.setKolicinaDugovna(0.1);
        stavka.setKapitalDugovniId(1L);

        stavka.setUgovor(new Ugovor());

        stavke.add(stavka);

        ugovor.setStavke(stavke);

        var valuta = new Valuta();
        valuta.setKodValute("USD");
        when(valutaRepository.getById(1L)).thenReturn(valuta);

        String token = "test";
        given(userService.getUserByToken(token)).willReturn(user);

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.of(ugovor));
        when(ugovorRepository.save(ugovor)).thenReturn(ugovor);

        assertEquals(ugovorService.rejectUgovor(ugovorId, token), ugovor);
        assertEquals(ugovor.getStatus(), UgovorStatus.REJECTED);
    }

    @ParameterizedTest
    @EnumSource(value = UgovorStatus.class, names = {"FINALIZED", "REJECTED"})
    void rejectUgovorFinalizedTest(UgovorStatus status) {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        Long ugovorId = 1L;

        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);
        ugovor.setStatus(status);

        String token = "test";
        given(userService.getUserByToken(token)).willReturn(user);

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.of(ugovor));

        assertThrows(ContractExpcetion.class, () -> ugovorService.rejectUgovor(ugovorId, token), "Ugovor is finalized");
    }

    @Test
    void rejectUgovorNoUgovorTest() {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        Long ugovorId = 1L;


        String token = "test";

        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.empty());

        assertThrows(ContractExpcetion.class, () -> ugovorService.rejectUgovor(ugovorId, token), "Ugovor not found");
    }

    @Test
    void getContractDocumentTest() {

        String documentId = "id";

        var contractDocument = new ContractDocument();
        var document = new Binary(BsonBinarySubType.BINARY, new byte[] {1,2,3});
        contractDocument.setDocument(document);

        when(contractDocumentService.getDocument(documentId)).thenReturn(contractDocument);

        assertEquals(ugovorService.getContractDocument(documentId), document);
    }

    @Test
    void getContractDocumentNoDocumentIdTest() {

        String documentId = null;

        assertThrows(ContractExpcetion.class, () -> ugovorService.getContractDocument(documentId), "Invalid document ID");
    }

    @Test
    void getContractDocumentNoDocumentTest() {

        String documentId = "id";

        when(contractDocumentService.getDocument(documentId)).thenReturn(null);

        assertThrows(ContractExpcetion.class, () -> ugovorService.getContractDocument(documentId), "Contract doesn't exist");
    }


    @Test
    void createTransakcionaStavkaTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        Long companyId = 1L;
        var company = new Company();
        company.setId(companyId);

        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);

        var request = new TransakcionaStavkaRequest();
        request.setUgovorId(1L);
        request.setKapitalTypePotrazni(KapitalType.NOVAC);
        request.setKapitalTypeDugovni(KapitalType.AKCIJA);
        request.setKapitalPotrazniOznaka("USD");
        request.setKapitalDugovniOznaka("USD");
        request.setKolicinaPotrazna(1.0);
        request.setKolicinaDugovna(1.0);

        var valuta = new Valuta();
        valuta.setKodValute("USD");
        valuta.setOznakaValute("USD");

        when(valutaRepository.findValutaByKodValute("USD")).thenReturn(valuta);
        when(valutaRepository.getById(any())).thenReturn(valuta);
        when(ugovorRepository.findById(ugovorId)).thenReturn(Optional.of(ugovor));
        when(transakcijaService.dodajTransakciju(eq(token), any())).thenReturn(new Transakcija());
        when(transakcionaStavkaRepository.save(any())).thenReturn(new TransakcionaStavka());

        var bidResponse = new AskBidPriceResponse();
        bidResponse.setAsk(1.0);
        bidResponse.setHartijaId(1L);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            utilities.when(() -> HttpUtils.getAskBidPrice(any(), any(), any())).thenReturn(ResponseEntity.ok(bidResponse));
            assertNotNull(ugovorService.addStavka(request, token));
        }
    }

    @Test
    void createTransakcionaStavkaBadRequestTest()
    {
        String token = "test";

        var request = new TransakcionaStavkaRequest();
        request.setUgovorId(null);
        request.setKapitalTypePotrazni(null);
        request.setKapitalTypeDugovni(null);
        request.setKapitalPotrazniOznaka(null);
        request.setKapitalDugovniOznaka(null);
        request.setKolicinaPotrazna(null);
        request.setKolicinaDugovna(null);

        assertThrows(ContractExpcetion.class, () -> ugovorService.addStavka(request, token), "bad request");
    }

    @ParameterizedTest
    @EnumSource(value = UgovorStatus.class, names = {"FINALIZED", "REJECTED"})
    void createTransakcionaStavkaUgovorFinalizedTest(UgovorStatus ugovorStatus)
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setStatus(ugovorStatus);
        ugovor.setId(ugovorId);


        var request = new TransakcionaStavkaRequest();
        request.setUgovorId(1L);
        request.setKapitalTypePotrazni(KapitalType.NOVAC);
        request.setKapitalTypeDugovni(KapitalType.AKCIJA);
        request.setKapitalPotrazniOznaka("USD");
        request.setKapitalDugovniOznaka("USD");
        request.setKolicinaPotrazna(1.0);
        request.setKolicinaDugovna(1.0);

        assertThrows(ContractExpcetion.class, () -> ugovorService.addStavka(request, token), "Ugovor is finalized");
    }

    @Test
    void modifyTransakcionaStavkaTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);

        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);

        var stavka = new TransakcionaStavka();
        stavka.setId(1L);
        stavka.setKapitalPotrazniOznaka("EUR");
        stavka.setKapitalDugovniOznaka("EUR");
        stavka.setKapitalTypePotrazni(KapitalType.AKCIJA);
        stavka.setKapitalPotrazniId(1L);
        stavka.setKapitalTypeDugovni(KapitalType.NOVAC);
        stavka.setKapitalDugovniId(1L);
        stavka.setKolicinaPotrazna(2.0);
        stavka.setKolicinaDugovna(2.0);
        stavka.setUgovor(ugovor);

        var request = new TransakcionaStavkaRequest();
        request.setStavkaId(1L);
        request.setKapitalTypePotrazni(KapitalType.NOVAC);
        request.setKapitalTypeDugovni(KapitalType.AKCIJA);
        request.setKapitalPotrazniOznaka("USD");
        request.setKapitalDugovniOznaka("USD");
        request.setKolicinaPotrazna(1.0);
        request.setKolicinaDugovna(1.0);

        var valuta = new Valuta();
        valuta.setKodValute("USD");
        valuta.setOznakaValute("USD");

        when(transakcionaStavkaRepository.findById(1L)).thenReturn(Optional.of(stavka));
        when(valutaRepository.findValutaByKodValute("USD")).thenReturn(valuta);
        when(valutaRepository.getById(any())).thenReturn(valuta);
        when(transakcijaService.dodajTransakciju(eq(token), any())).thenReturn(new Transakcija());
        when(transakcionaStavkaRepository.save(any())).thenReturn(new TransakcionaStavka());

        var bidResponse = new AskBidPriceResponse();
        bidResponse.setAsk(1.0);
        bidResponse.setHartijaId(1L);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            utilities.when(() -> HttpUtils.getAskBidPrice(any(), any(), any())).thenReturn(ResponseEntity.ok(bidResponse));
            assertNotNull(ugovorService.modifyStavka(request, token));
        }
    }

    @Test
    void modifyTransakcionaStavkaBadRequestTest()
    {

        var request = new TransakcionaStavkaRequest();

        var token = "test";

        assertThrows( ContractExpcetion.class, () -> ugovorService.modifyStavka(request, token), "bad request");
    }

    @ParameterizedTest
    @EnumSource(value = UgovorStatus.class, names = {"FINALIZED", "REJECTED"})
    void modifyTransakcionaStavkaTestFinalized(UgovorStatus ugovorStatus)
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);


        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setStatus(ugovorStatus);
        ugovor.setId(ugovorId);

        var stavka = new TransakcionaStavka();
        stavka.setId(1L);
        stavka.setKapitalPotrazniOznaka("EUR");
        stavka.setKapitalDugovniOznaka("EUR");
        stavka.setKapitalTypePotrazni(KapitalType.AKCIJA);
        stavka.setKapitalPotrazniId(1L);
        stavka.setKapitalTypeDugovni(KapitalType.NOVAC);
        stavka.setKapitalDugovniId(1L);
        stavka.setKolicinaPotrazna(2.0);
        stavka.setKolicinaDugovna(2.0);
        stavka.setUgovor(ugovor);

        var request = new TransakcionaStavkaRequest();
        request.setStavkaId(1L);
        request.setKapitalTypePotrazni(KapitalType.NOVAC);
        request.setKapitalTypeDugovni(KapitalType.AKCIJA);
        request.setKapitalPotrazniOznaka("USD");
        request.setKapitalDugovniOznaka("USD");
        request.setKolicinaPotrazna(1.0);
        request.setKolicinaDugovna(1.0);

        when(transakcionaStavkaRepository.findById(1L)).thenReturn(Optional.of(stavka));

        var bidResponse = new AskBidPriceResponse();
        bidResponse.setAsk(1.0);
        bidResponse.setHartijaId(1L);

        assertThrows( ContractExpcetion.class, () -> ugovorService.modifyStavka(request, token), "Ugovor is finalized");
    }

    @Test
    void removeTransakcionaStavkaTest()
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);

        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setId(ugovorId);

        var stavka = new TransakcionaStavka();
        stavka.setId(1L);
        stavka.setKapitalPotrazniOznaka("EUR");
        stavka.setKapitalDugovniOznaka("EUR");
        stavka.setKapitalTypePotrazni(KapitalType.AKCIJA);
        stavka.setKapitalPotrazniId(1L);
        stavka.setKapitalTypeDugovni(KapitalType.NOVAC);
        stavka.setKapitalDugovniId(1L);
        stavka.setKolicinaPotrazna(2.0);
        stavka.setKolicinaDugovna(2.0);
        stavka.setUgovor(ugovor);

        var valuta = new Valuta();
        valuta.setKodValute("USD");
        valuta.setOznakaValute("USD");

        when(transakcionaStavkaRepository.findById(1L)).thenReturn(Optional.of(stavka));
        when(transakcijaService.dodajTransakciju(eq(token), any())).thenReturn(new Transakcija());

        var bidResponse = new AskBidPriceResponse();
        bidResponse.setAsk(1.0);
        bidResponse.setHartijaId(1L);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            utilities.when(() -> HttpUtils.getAskBidPrice(any(), any(), any())).thenReturn(ResponseEntity.ok(bidResponse));
            assertNotNull(ugovorService.removeStavka(1L, token));
        }
    }

    @ParameterizedTest
    @EnumSource(value = UgovorStatus.class, names = {"FINALIZED", "REJECTED"})
    void removeTransakcionaStavkaFinalizedTest(UgovorStatus ugovorStatus)
    {
        Long userId = 1L;
        var user = new UserDto();
        user.setId(userId);
        user.setRoleName("ROLE_GL_ADMIN");

        String token = "test";

        given(userService.getUserByToken(token)).willReturn(user);

        var ugovorId = 1L;
        var ugovor = new Ugovor();
        ugovor.setStatus(ugovorStatus);
        ugovor.setId(ugovorId);

        var stavka = new TransakcionaStavka();
        stavka.setId(1L);
        stavka.setKapitalPotrazniOznaka("EUR");
        stavka.setKapitalDugovniOznaka("EUR");
        stavka.setKapitalTypePotrazni(KapitalType.AKCIJA);
        stavka.setKapitalPotrazniId(1L);
        stavka.setKapitalTypeDugovni(KapitalType.NOVAC);
        stavka.setKapitalDugovniId(1L);
        stavka.setKolicinaPotrazna(2.0);
        stavka.setKolicinaDugovna(2.0);
        stavka.setUgovor(ugovor);

        var valuta = new Valuta();
        valuta.setKodValute("USD");
        valuta.setOznakaValute("USD");

        when(transakcionaStavkaRepository.findById(1L)).thenReturn(Optional.of(stavka));

        var bidResponse = new AskBidPriceResponse();
        bidResponse.setAsk(1.0);
        bidResponse.setHartijaId(1L);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            utilities.when(() -> HttpUtils.getAskBidPrice(any(), any(), any())).thenReturn(ResponseEntity.ok(bidResponse));
            assertThrows( ContractExpcetion.class, () -> ugovorService.removeStavka(1L, token), "Ugovor is finalized");
        }
    }
}
