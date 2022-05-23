package rs.edu.raf.banka.user_service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.PasswordTokenRepository;
import rs.edu.raf.banka.user_service.repository.RoleRepository;
import rs.edu.raf.banka.user_service.repository.UserRepository;
import rs.edu.raf.banka.user_service.service.implementation.UserServiceImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImplementation userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordTokenRepository passwordTokenRepository;

    private CreateUserForm createUserForm = initUserMockForm();

    @Test
    void testGetUser() {
        User user = new User("UserX", "X");
        given(userRepository.findByUsername("UserX")).willReturn(Optional.of(user));

        assertEquals(user, userService.getUser("UserX"));
    }

    @Test
    void testGetRole(){
        Role role = new Role();
        given(roleRepository.findByName("dummyRole")).willReturn(role);

        assertEquals(role, userService.getRole("dummyRole"));
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

        assertEquals(true, passwordEncoder.matches("mockPass123", user.getPassword()));
    }

    @Test
    void testInvalidPasswordChangePassword() {
        User user = new User("UserX", "X");
        user.setEmail("user@mock");

        userService.changePassword("mockPass", user);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertEquals(false, passwordEncoder.matches("mockPass", user.getPassword()));
    }

   /*@Test
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
    }*/

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

        assertEquals(true,userService.hasEditPermissions(user, token));
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

        assertEquals(false,userService.hasEditPermissions(user, token));
    }

    @Test
    void testHasEditPermissionsFunc() {
        assertEquals(true,userService.hasEditPermission(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}, Permissions.MY_EDIT));
    }

    @Test
    void testHasNoEditPermissionsFunc() {
        assertEquals(false,userService.hasEditPermission(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "DELETE_USER"}, Permissions.MY_EDIT));
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

        assertEquals(user.getId(),userService.getUserId(token));
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

        assertEquals(user,userService.getUserByToken(token));
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

        Throwable exception = assertThrows(BadCredentialsException.class, () -> userService.getUserByToken(token));
        assertEquals("Bad credentials", exception.getMessage());
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

        Throwable exception = assertThrows(BadCredentialsException.class, () -> userService.getUserByToken(token));
        assertEquals("Token is invalid", exception.getMessage());
    }

    @Test
    void testSetRoleToUser() {
        User user = new User("UserX", "X");
        user.setId(2L);
        user.setEmail("user@mock");
        user.setAktivan(true);
        Role r = new Role();
        r.setName("admin");

        when(userRepository.findByUsername("userX")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("admin")).thenReturn(r);
        userService.setRoleToUser("userX","admin");

        assertEquals(user.getRole(), r);
    }

    @Test
    void testEditOtpSecret() {
        User user = new User("UserX", "X");
        user.setId(2L);
        user.setEmail("user@mock");
        user.setAktivan(true);

        userService.editOtpSeecret(user,"secret");
        assertEquals("secret",user.getOtpSeecret());
    }

    @Test
    void testGetUserByEmailInvalid() {
        given(userRepository.findByEmail("user@mock")).willReturn(Optional.empty());

        assertEquals(null, userService.getUserByEmail("user@mock"));
    }

    @Test
    void testCreateUser() {
        User user = new User("dummyname.test", createUserForm.getIme() + "Test123");
        user.setId(0L);

        when(userRepository.save(any())).thenReturn(user);

        assertEquals(user, userService.createUser(createUserForm));
    }

    @Test
    void testCreateUserWithExistingUsername() {
        User existingUser = new User("dummyname.test", createUserForm.getIme() + "Test123");
        existingUser.setId(0L);
        User user = new User("dummyname.test", createUserForm.getIme() + "Test123");
        user.setId(2L);

        when(userService.getUser(user.getUsername())).thenReturn(any());

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(existingUser));

        when(userRepository.save(any())).thenReturn(user);

        assertEquals(user, userService.createUser(createUserForm));
    }

    private CreateUserForm initUserMockForm() {
        CreateUserForm userMockForm = new CreateUserForm();
        userMockForm.setIme("dummyName");
        userMockForm.setPrezime("Test");
        userMockForm.setEmail("mock@test");
        userMockForm.setJmbg("123");
        userMockForm.setBrTelefon("123");
        userMockForm.setPozicija("ROLE_ADMIN");

        return userMockForm;
    }

}
