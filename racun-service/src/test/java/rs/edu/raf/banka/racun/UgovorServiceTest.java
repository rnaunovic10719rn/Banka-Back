package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.exceptions.ContractExpcetion;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.contract.UgovorRepository;
import rs.edu.raf.banka.racun.service.impl.UgovorService;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UgovorServiceTest {

    @InjectMocks
    UgovorService ugovorService;

    @Mock
    UgovorRepository ugovorRepository;

    @Mock
    UserService userService;



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
}
