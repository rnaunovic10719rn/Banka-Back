package rs.edu.raf.banka.user_service;

import ch.qos.logback.core.joran.action.NewRuleAction;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.edu.raf.banka.user_service.controller.UserController;
import rs.edu.raf.banka.user_service.controller.response_forms.*;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.security.OTPUtilities;
import rs.edu.raf.banka.user_service.service.implementation.UserServiceImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserServiceImplementation userServiceImplementation;

    @Autowired
    private ObjectMapper objectMapper;

    String validJWToken = initValidJWT();
    String invalidJWToken = initInvalidJWT();
    CreateUserForm userMockForm = initUserMockForm();
    ResetPasswordForm resetPasswordForm = initResetPasswordForm();
    ChangePasswordForm changePasswordForm = initChangePasswordForm();
    OtpToSecretForm otpToSecretForm = initOtpToSecretForm();
    OtpQRForm otpQRForm = initOtpQRFormForm();
    ChangePasswordForm badChangePasswordForm = initBadChangePasswordForm();
    NewPasswordForm newPasswordForm = initNewPasswordForm();
    String dummyName = "Mock";

    @Test
    void testGetUsersAPI() throws Exception {
        List<User> users = new ArrayList<>();
        User user = new User(dummyName, "Test");
        users.add(user);

        String jsonResult = "[{\"id\":0,\"username\":\"Mock\",\"ime\":null,\"prezime\":null,\"email\":null,\"jmbg\":null,\"brTelefon\":null,\"password\":\"Test\",\"otpSeecret\":null,\"aktivan\":false,\"role\":null,\"requiresOtp\":false}]";

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
        String jsonResult = "{\"id\":0,\"username\":\"Mock\",\"ime\":null,\"prezime\":null,\"email\":null,\"jmbg\":null,\"brTelefon\":null,\"password\":\"Test\",\"otpSeecret\":null,\"aktivan\":false,\"role\":null,\"requiresOtp\":false}";

        when(userServiceImplementation.createUser(userMockForm)).thenReturn(new User(dummyName, "Test"));


        mockMvc.perform(post("/api/user/create").header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userMockForm)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResult));
    }

    @Test
    void testEditPermissionsAPI() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        given(userServiceImplementation.getUserById(2L)).willReturn(Optional.of(user));

        when(userServiceImplementation.hasEditPermissions(user, "Bearer " + validJWToken)).thenReturn(true);

        doNothing()
            .when(userServiceImplementation)
            .editUser(user, userMockForm);

        mockMvc.perform(post("/api/user/edit/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userMockForm)))
                .andExpect(status().isOk())
                .andExpect(content().string(dummyName + " edited"));
    }

    @Test
    void testInvalidEditPermissions() throws Exception{
        mockMvc.perform(post("/api/user/edit/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testDeleteAPI() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        given(userServiceImplementation.getUserById(2L)).willReturn(Optional.of(user));

        when(userServiceImplementation.deleteUser(user)).thenReturn(true);

        mockMvc.perform(delete("/api/user/delete/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().isOk())
                .andExpect(content().string(dummyName + " disabled"));
    }

    @Test
    void testInvalidDeleteAPI() throws Exception{
        given(userServiceImplementation.getUserById(2L)).willReturn(Optional.ofNullable(null));

        mockMvc.perform(delete("/api/user/delete/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetUser() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        when(userServiceImplementation.getUserByToken(anyString())).thenReturn(user);

        mockMvc.perform(get("/api/user", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidGetUser() throws Exception{
        when(userServiceImplementation.getUserByToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/user", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInvalidAuthGetUser() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        when(userServiceImplementation.getUserByToken(anyString())).thenReturn(user);

        mockMvc.perform(get("/api/user", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserId() throws Exception{
        long id = 2L;
        String token = "dummyToken";

        when(userServiceImplementation.getUserId(token)).thenReturn(id);

        mockMvc.perform(post("/api/user/getId/{token}", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidGetUserId() throws Exception{
        String invalidToken = "dummyToken";

        when(userServiceImplementation.getUserId(invalidToken)).thenReturn(null);

        mockMvc.perform(post("/api/user/getId/{token}", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordInternal() throws Exception{
        long id = 2L;
        User user = new User(dummyName, "Test");

        when(userServiceImplementation.getUserById(id)).thenReturn(Optional.of(user));

        when(userServiceImplementation.changePassword("mockPass", user)).thenReturn(true);

        mockMvc.perform(post("/api/user/new-password/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newPasswordForm)))
                .andExpect(status().isOk());
    }

    @Test
    void testChangeBadPasswordInternal() throws Exception{
        long id = 2L;
        User user = new User(dummyName, "Test");

        when(userServiceImplementation.getUserById(id)).thenReturn(Optional.of(user));

        when(userServiceImplementation.changePassword("mockPass", user)).thenReturn(false);

        mockMvc.perform(post("/api/user/new-password/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newPasswordForm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidChangeBadPasswordInternal() throws Exception{
        long id = 2L;

        when(userServiceImplementation.getUserById(id)).thenReturn(Optional.ofNullable(null));

        mockMvc.perform(post("/api/user/new-password/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newPasswordForm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditUserFromToken() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        when(userServiceImplementation.hasEditPermissions(any(),anyString())).thenReturn(true);
        when(userServiceImplementation.getUserByToken(anyString())).thenReturn(user);

        mockMvc.perform(patch("/api/user", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().isOk());
    }

    @Test
    void testEditUserFromTokenInvalid() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        when(userServiceImplementation.hasEditPermissions(any(),anyString())).thenReturn(true);
        when(userServiceImplementation.getUserByToken(anyString())).thenReturn(user);

        mockMvc.perform(patch("/api/user", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditUserFromInvalidToken() throws Exception{
        mockMvc.perform(patch("/api/user", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testResetPassword() throws Exception{
        when(userServiceImplementation.resetPassword("mock@test")).thenReturn(true);

        mockMvc.perform(post("/api/user/reset-password", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(resetPasswordForm)))
                .andExpect(status().isOk())
                .andExpect(content().string("Mail send to: " + resetPasswordForm.getEmail()));
    }

    @Test
    void testInvalidResetPassword() throws Exception{
        mockMvc.perform(post("/api/user/reset-password", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(resetPasswordForm)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Mail failed to send"));
    }

    @Test
    void testChangePassword() throws Exception{

        when(userServiceImplementation.setNewPassword(anyString(),anyString())).thenReturn(true);

        mockMvc.perform(post("/api/user/change-password", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(changePasswordForm)))
                .andExpect(status().isOk())
                .andExpect(content().string("New password!"));

    }

    @Test
    void testInvalidChangePassword() throws Exception{
        mockMvc.perform(post("/api/user/change-password", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(badChangePasswordForm)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Invalid token!"));
    }
    

    @Test
    void testInvalidNewPassword() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        when(userServiceImplementation.getUserById(2L)).thenReturn(null);
        when(userServiceImplementation.changePassword(any(),any())).thenReturn(true);

        mockMvc.perform(post("/api/user/new-password/", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newPasswordForm)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testOtpGenerateSecret() throws Exception{
        mockMvc.perform(get("/api/otp/generateSecret")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testOtpGenerateQrImage() throws Exception{
        mockMvc.perform(post("/api/otp/generateQrImage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(otpQRForm)))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidOtpGenerateQrImage() throws Exception{
        mockMvc.perform(post("/api/otp/generateQrImage")
                        .contentType(MediaType.APPLICATION_JSON)
                       )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOtpGenerateQrUri() throws Exception{
        mockMvc.perform(post("/api/otp/generateQrUri")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(otpQRForm)))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidOtpGenerateQrUri() throws Exception{
        mockMvc.perform(post("/api/otp/generateQrUri")
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testValidate() throws Exception{
        mockMvc.perform(post("/api/otp/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(otpToSecretForm)))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidValidate() throws Exception{
        mockMvc.perform(post("/api/otp/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void tesInvalidSetOtp() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        when(userServiceImplementation.getUserById(2L)).thenReturn(Optional.of(user));
        when(userServiceImplementation.hasEditPermissions(user,"Bearer " + validJWToken)).thenReturn(true);
        //doNothing().when(userServiceImplementation.editOtpSeecret(user, any()));

        mockMvc.perform(post("/api/otp/set/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("secret")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidEditPermissionSetOtp() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);

        when(userServiceImplementation.getUserById(2L)).thenReturn(Optional.ofNullable(user));
        when(userServiceImplementation.hasEditPermissions(user,"Bearer " + validJWToken)).thenReturn(false);

        mockMvc.perform(post("/api/otp/set/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("secret")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEmptySetOtp() throws Exception{
        long id = 2L;

        when(userServiceImplementation.getUserById(id)).thenReturn(Optional.ofNullable(null));

        mockMvc.perform(post("/api/otp/set/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("secret")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tesClearOtp() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));
        user.setRequiresOtp(false);

        when(userServiceImplementation.getUserById(2L)).thenReturn(Optional.of(user));
        when(userServiceImplementation.hasEditPermissions(user,"Bearer " + validJWToken)).thenReturn(true);

        //doNothing().when(userServiceImplementation.editOtpSeecret(user, any()));

        mockMvc.perform(post("/api/otp/clear/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testEmptyClearOtp() throws Exception{
        long id = 2L;

        when(userServiceImplementation.getUserById(2L)).thenReturn(Optional.ofNullable(null));

        mockMvc.perform(post("/api/otp/clear/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidPermissionsClearOtp() throws Exception{
        User user = new User(dummyName, "Test");
        long id = 2L;

        when(userServiceImplementation.getUserById(2L)).thenReturn(Optional.of(user));

        when(userServiceImplementation.hasEditPermissions(user, validJWToken)).thenReturn(false);

        mockMvc.perform(post("/api/otp/clear/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tesInvalidClearOtp() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));
        user.setRequiresOtp(true);

        when(userServiceImplementation.getUserById(2L)).thenReturn(Optional.of(user));
        when(userServiceImplementation.hasEditPermissions(user,"Bearer " + validJWToken)).thenReturn(true);

        //doNothing().when(userServiceImplementation.editOtpSeecret(user, any()));

        mockMvc.perform(post("/api/otp/clear/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOtpRequires() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        user.setOtpSeecret("secret");
        when(userServiceImplementation.getUser("mockUsername")).thenReturn(user);

        mockMvc.perform(post("/api//otp/requires/{username}","mockUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testOtpRequiresTrue() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        user.setRequiresOtp(true);
        when(userServiceImplementation.getUser("mockUsername")).thenReturn(user);

        mockMvc.perform(post("/api/otp/requires/{username}","mockUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    void testOtpRequiresFalse() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        user.setRequiresOtp(false);
        when(userServiceImplementation.getUser("mockUsername")).thenReturn(user);

        mockMvc.perform(post("/api/otp/requires/{username}","mockUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("false"));
    }

    @Test
    void testOtpHas() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        user.setOtpSeecret("secret");
        when(userServiceImplementation.getUser("mockUsername")).thenReturn(user);

        mockMvc.perform(post("/api/otp/has/{username}","mockUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testOtpHasTrue() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        user.setOtpSeecret("secret");
        when(userServiceImplementation.getUser("mockUsername")).thenReturn(user);

        mockMvc.perform(post("/api/otp/has/{username}","mockUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    void testOtpHasFalse() throws Exception{
        User user = new User(dummyName, "Test");
        user.setId(2L);
        user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

        user.setOtpSeecret(null);
        when(userServiceImplementation.getUser("mockUsername")).thenReturn(user);

        mockMvc.perform(post("/api/otp/has/{username}","mockUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("false"));
    }


    @Test
    void testInvalidDelete() throws Exception{
        mockMvc.perform(delete("/api/user/delete/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testInvalidAdminDelete() throws Exception{
        User user = new User("admin", "Test");
        user.setId(1L);
        user.setRole(new Role(null,"GL_ADMIN_ROLE", List.of(new String[]{"GL_ADMIN_MOCK"})));

        given(userServiceImplementation.getUserById(1L)).willReturn(Optional.of(user));
        when(userServiceImplementation.deleteUser(user)).thenReturn(false);


        mockMvc.perform(delete("/api/user/delete/{id}", 1L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userMockForm)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Can't delete admin"));
    }

    @Test
    void testInvalidEditUser() throws Exception{
        long id = 2L;

        when(userServiceImplementation.getUserById(id)).thenReturn(Optional.ofNullable(null));

        mockMvc.perform(post("/api/user/edit/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userMockForm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidPermissionsForEdit() throws Exception{
            User user = new User(dummyName, "Test");
            user.setId(2L);
            user.setRole(new Role(null,"ADMIN_ROLE", List.of(new String[]{"ADMIN_MOCK"})));

            given(userServiceImplementation.getUserById(2L)).willReturn(Optional.of(user));

            when(userServiceImplementation.hasEditPermissions(user, "Bearer " + invalidJWToken)).thenReturn(false);

            mockMvc.perform(post("/api/user/edit/{id}", 2L).header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(userMockForm)))
                    .andExpect(status().isBadRequest());
    }

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
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    String initInvalidJWT(){
        return JWT.create()
                .withSubject(dummyName)
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"X_LIST_USERS", "DUMMY_FAKE_PERMISSION"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    private CreateUserForm initUserMockForm() {
        CreateUserForm userMockForm = new CreateUserForm();
        userMockForm.setIme(dummyName);
        userMockForm.setPrezime("Test");
        userMockForm.setEmail("mock@test");
        userMockForm.setJmbg("123");
        userMockForm.setBrTelefon("123");
        userMockForm.setPozicija("ROLE_ADMIN");

        return userMockForm;
    }

    private ResetPasswordForm initResetPasswordForm() {
        ResetPasswordForm resetPassMockForm = new ResetPasswordForm();
        resetPassMockForm.setEmail("mock@test");
        return resetPassMockForm;
    }

    private ChangePasswordForm initChangePasswordForm() {
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setNewPassword("mockPass123");
        changePasswordForm.setEmailToken("Bearer mocken");
        return  changePasswordForm;
    }

    private OtpQRForm initOtpQRFormForm() {
        OtpQRForm otpQRForm = new OtpQRForm();
        otpQRForm.setLabel("mockLabel");
        otpQRForm.setSecret("mockSecret");
        return  otpQRForm;
    }

    private OtpToSecretForm initOtpToSecretForm() {
        OtpToSecretForm otpToSecretForm = new OtpToSecretForm();
        otpToSecretForm.setOtp("mockOtp");
        otpToSecretForm.setSecret("mockSecret");
        return  otpToSecretForm;
    }
  
    private NewPasswordForm initNewPasswordForm(){
        NewPasswordForm newUserPasswordForm = new NewPasswordForm();
        newUserPasswordForm.setNewPassword("mockPass");
        return newUserPasswordForm;
    }

    private ChangePasswordForm initBadChangePasswordForm() {
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setNewPassword("mockPass");
        changePasswordForm.setEmailToken("mocken");
        return  changePasswordForm;
    }
  
}
