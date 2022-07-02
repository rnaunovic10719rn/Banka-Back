package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.exceptions.ContractExpcetion;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.repository.contract.UgovorRepository;
import rs.edu.raf.banka.racun.service.impl.UgovorService;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token));
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

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token));
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

        assertThrows(ContractExpcetion.class , () -> ugovorService.getUgovorById(ugovorId, token));
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
    void testGetAllByNoCompany()
    {
        String token = "test";
        Long companyId = 1L;

        given(companyRepository.findById(companyId)).willReturn(Optional.empty());

        assertThrows(ContractExpcetion.class , () -> ugovorService.getAllByCompany(companyId, token));
        assertThrows(ContractExpcetion.class , () -> ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.DRAFT));
        assertThrows(ContractExpcetion.class , () -> ugovorService.getAllByCompanyAndUgovorStatus(companyId, token, UgovorStatus.FINALIZED));
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

}
