package rs.edu.raf.banka.user_service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.assertj.core.util.Arrays;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.mail.PasswordResetToken;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.PasswordTokenRepository;
import rs.edu.raf.banka.user_service.repository.UserRepository;
import rs.edu.raf.banka.user_service.service.implementation.UserServiceImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImplementation userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordTokenRepository passwordTokenRepository;

    @Test
    void testGetUser() {
        User user = new User("UserX", "X");
        given(userRepository.findByUsername("UserX")).willReturn(Optional.of(user));

        assertEquals(user, userService.getUser("UserX"));
    }

    @Test
    void testGetUserInvalid() {
        given(userRepository.findByUsername("UserY")).willReturn(Optional.empty());

        assertEquals(null, userService.getUser("UserY"));
    }

    @Test
    void testFindUserById() {
        Optional<User> optUser = Optional.of(new User());
        given(userRepository.findById(1L)).willReturn(optUser);

        assertEquals(optUser, userService.getUserById(1L));
    }

    @Test
    void testGetUsers() {
        List<User> userList = new ArrayList<>();
        given(userRepository.findAll()).willReturn(userList);

        assertEquals(userList, userService.getUsers());
    }

    @Test
    void testDeleteUser() {
        User user = new User("UserX", "X");

        List<String> mockPermissions = new ArrayList<>();
        mockPermissions.add("mock_permission");
        user.setRole(new Role(null, "ROLE_ADMIN", mockPermissions));
        user.setAktivan(true);

        userService.deleteUser(user);

        assertEquals(false, user.isAktivan());
    }

    @Test
    void testDeleteAdminFail() {
        User user = new User("UserX", "X");

        List<String> mockPermissions = new ArrayList<>();
        mockPermissions.add("mock_permission");
        user.setRole(new Role(null, "ROLE_GL_ADMIN", mockPermissions));
        user.setAktivan(true);

        userService.deleteUser(user);

        assertEquals(true, user.isAktivan());
    }

    @Test
    void testGetUserByEmail() {
        User user = new User("UserX", "X");
        user.setEmail("user@mock");
        given(userRepository.findByEmail("user@mock")).willReturn(Optional.of(user));

        assertEquals(user, userService.getUserByEmail("user@mock"));
    }

    @Test
    void testChangePassword() {
        User user = new User("UserX", "X");
        user.setEmail("user@mock");

        userService.changePassword("mockPass123", user);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertEquals(passwordEncoder.matches("mockPass123", user.getPassword()), true);
    }

    @Test
    void testInvalidPasswordChangePassword() {
        User user = new User("UserX", "X");
        user.setEmail("user@mock");

        userService.changePassword("mockPass", user);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertEquals(passwordEncoder.matches("mockPass", user.getPassword()), false);
    }

    @Test
    void testSetNewPassword() {
        String token = "token";
        User user = new User("UserX", "X");
        user.setEmail("user@mock");

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);

        given(passwordTokenRepository.findByToken(token)).willReturn(passwordResetToken);

        userService.setNewPassword("mockPass123", "token");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertEquals(passwordEncoder.matches("mockPass123", user.getPassword()), true);
    }

    @Test
    void testSetNewInvalidPassword() {
        String token = "token";
        User user = new User("UserX", "X");
        user.setEmail("user@mock");

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);

        given(passwordTokenRepository.findByToken(token)).willReturn(passwordResetToken);

        Throwable exception = assertThrows(BadCredentialsException.class, () -> userService.setNewPassword("mockPass", "token"));
        assertEquals("Password: must have 8 characters,one uppercase and one digit minimum", exception.getMessage());
    }

    @Test
    void testHasEditPermisions() {
        String token = JWT.create()
                .withSubject("userX")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
        User user = new User("userX", "Test");
        user.setId(2L);
        user.setRole(new Role(null, "ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        assertEquals(userService.hasEditPermissions(user, token), true);
    }

    @Test
    void testHasNoEditPermisions() {
        String token = JWT.create()
                .withSubject("userX")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
        User user = new User("userX", "Test");
        user.setId(2L);

        assertEquals(userService.hasEditPermissions(user, token), false);
    }

    @Test
    void testHasEditPermissionsFunc() {
        assertEquals(userService.hasEditPermission(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}, Permissions.MY_EDIT), true);
    }

    @Test
    void testHasNoEditPermissionsFunc() {
        assertEquals(userService.hasEditPermission(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "DELETE_USER"}, Permissions.MY_EDIT), false);
    }

    @Test
    void testGetUserId() {
        User user = new User("UserX", "X");
        user.setId(2L);
        user.setEmail("user@mock");

        String token = JWT.create()
                .withSubject("userX")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));

        when(userRepository.findByUsername("userX")).thenReturn(Optional.of(user));

        assertEquals(userService.getUserId(token), user.getId());
    }

    @Test
    void testGetUserByToken() {
        User user = new User("UserX", "X");
        user.setId(2L);
        user.setEmail("user@mock");
        user.setAktivan(true);

        String token = JWT.create()
                .withSubject("userX")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));

        when(userRepository.findByUsername("userX")).thenReturn(Optional.of(user));

        assertEquals(userService.getUserByToken(token), user);
    }

    @Test
    void testIvalidGetUserByToken() {
        User user = new User("UserX", "X");
        user.setId(2L);
        user.setEmail("user@mock");
        user.setAktivan(true);

        String token = JWT.create()
                .withSubject("userX")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));

        Throwable exception = assertThrows(UsernameNotFoundException.class, () -> userService.getUserByToken(token));
        assertEquals("User not found in database", exception.getMessage());
    }

    @Test
    void testIvalidTokenGetUserByToken() {
        User user = new User("UserX", "X");
        user.setId(2L);
        user.setEmail("user@mock");
        user.setAktivan(true);

        String token = JWT.create()
                .withSubject("userX")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret2".getBytes()));

        Throwable exception = assertThrows(UsernameNotFoundException.class, () -> userService.getUserByToken(token));
        assertEquals("Token is invalid", exception.getMessage());
    }


    @Test
    void testGetUserByEmailInvalid() {
        given(userRepository.findByEmail("user@mock")).willReturn(Optional.empty());

        assertEquals(null, userService.getUserByEmail("user@mock"));
    }

}
