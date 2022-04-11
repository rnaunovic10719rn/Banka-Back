package rs.edu.raf.banka.user_service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import rs.edu.raf.banka.user_service.controller.UserController;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.UserRepository;
import rs.edu.raf.banka.user_service.security.SecurityConfiguration;
import rs.edu.raf.banka.user_service.service.UserService;
import rs.edu.raf.banka.user_service.service.implementation.UserServiceImplementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserServiceImplementation userServiceImplementation;

    @Mock
    UserRepository userRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    String validJWToken = initValidJWT();
    String invalidJWToken = initInvalidJWT();
    String dummyName = "Mock";

    public void setup()
    {
        //Init MockMvc Object and build
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetUsersAPI() throws Exception {
        List<User> users = new ArrayList<>();
        User user = new User(dummyName, "Test");
        users.add(user);

        String jsonResult = "[{\"id\":0,\"username\":\"Mock\",\"ime\":null,\"prezime\":null,\"email\":null,\"jmbg\":null,\"br_telefon\":null,\"password\":\"Test\",\"otpSeecret\":null,\"aktivan\":false,\"role\":null,\"requiresOtp\":false}]";

        when(userServiceImplementation.getUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResult));
    }

    @Test
    void testGetUsersInvalidToken() throws Exception{
        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJWToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateUserAPI() throws Exception {
        CreateUserForm userMockForm = new CreateUserForm();
        userMockForm.setIme(dummyName);
        userMockForm.setPrezime("Test");
        userMockForm.setEmail("mock@test");
        userMockForm.setJmbg("123");
        userMockForm.setBr_telefon("123");
        userMockForm.setPozicija("ROLE_ADMIN");

        String jsonResult = "{\"id\":0,\"username\":\"Mock\",\"ime\":null,\"prezime\":null,\"email\":null,\"jmbg\":null,\"br_telefon\":null,\"password\":\"Test\",\"otpSeecret\":null,\"aktivan\":false,\"role\":null,\"requiresOtp\":false}";

        when(userServiceImplementation.createUser(userMockForm)).thenReturn(new User(dummyName, "Test"));

        mockMvc.perform(post("/api/user/create").header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userMockForm)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResult));
    }

   /* @Test
    void testHasEditPermissionsAPI() throws Exception{
        CreateUserForm userMockForm = new CreateUserForm();
        userMockForm.setIme(dummyName);
        userMockForm.setPrezime("Test");
        userMockForm.setEmail("mock@test");
        userMockForm.setJmbg("123");
        userMockForm.setBr_telefon("123");
        userMockForm.setPozicija("ROLE_ADMIN");

        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"EDIT_USER", "k"})));

        when(userServiceImplementation.hasEditPermissions(user, validJWToken)).thenReturn(true);

        doNothing()
            .when(userServiceImplementation)
            .editUser(user, userMockForm);

        mockMvc.perform(post("/api/user/edit/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userMockForm)))
                .andExpect(status().isOk());
    }*/

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String initValidJWT(){
        return JWT.create()
                .withSubject(dummyName)
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    String initInvalidJWT(){
        return JWT.create()
                .withSubject(dummyName)
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"X_LIST_USERS", "DUMMY_FAKE_PERMISSION"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }
}
