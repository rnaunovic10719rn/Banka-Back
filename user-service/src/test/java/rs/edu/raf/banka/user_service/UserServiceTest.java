package rs.edu.raf.banka.user_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.RoleRepository;
import rs.edu.raf.banka.user_service.repository.UserRepository;
import rs.edu.raf.banka.user_service.service.implementation.UserServiceImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImplementation userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void testGetUser() {
        User user = new User("UserX","X");
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
        User user = new User("UserX","X");

        List<String> mockPermissions = new ArrayList<>();
        mockPermissions.add("mock_permission");
        user.setRole(new Role(null, "ROLE_ADMIN", mockPermissions));
        user.setAktivan(true);

        userService.deleteUser(user);

        assertEquals(false, user.isAktivan());
    }

    @Test
    void testDeleteAdminFail() {
        User user = new User("UserX","X");

        List<String> mockPermissions = new ArrayList<>();
        mockPermissions.add("mock_permission");
        user.setRole(new Role(null, "ROLE_GL_ADMIN", mockPermissions));
        user.setAktivan(true);

        userService.deleteUser(user);

        assertEquals(true, user.isAktivan());
    }

    @Test
    void testGetUserByEmail() {
        User user = new User("UserX","X");
        user.setEmail("user@mock");
        given(userRepository.findByEmail("user@mock")).willReturn(Optional.of(user));

        assertEquals(user, userService.getUserByEmail("user@mock"));
    }

    @Test
    void testChangePassword(){
        User user = new User("UserX","X");
        user.setEmail("user@mock");

        userService.changePassword("mockPass123",user);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertEquals(passwordEncoder.matches("mockPass123", user.getPassword()), true);
    }

    @Test
    void testInvalidPasswordChangePassword(){
        User user = new User("UserX","X");
        user.setEmail("user@mock");

        userService.changePassword("mockPass",user);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertEquals(passwordEncoder.matches("mockPass", user.getPassword()), false);
    }

    @Test
    void testGetUserByEmailInvalid() {
        given(userRepository.findByEmail("user@mock")).willReturn(Optional.empty());

        assertEquals(null, userService.getUserByEmail("user@mock"));
    }

}
