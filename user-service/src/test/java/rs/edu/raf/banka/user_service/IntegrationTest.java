package rs.edu.raf.banka.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisabledIfEnvironmentVariable(named = "WORKSPACE", matches = "CI")
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"Admin123\"}"))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        token = mvcResult.getResponse().getContentAsString().replace("\"", "");

    }

    @Test
    void createAndVerifyUser() throws Exception {
        CreateUserForm cuf = new CreateUserForm();
        cuf.setIme("Test");
        cuf.setPrezime("Testic");
        cuf.setEmail("test@raf.rs");
        cuf.setJmbg("1234567980123");
        cuf.setBrTelefon("0690000000");
        cuf.setPozicija("ROLE_ADMIN");
        cuf.setLimit(0.0);
        cuf.setNeedsSupervisorPermission(false);

        mockMvc.perform(post("/api/user/create")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(cuf)))
                .andExpect(status().isOk());

        Optional<User> user = userRepository.findByEmail("test@raf.rs");
        assertThat(user.isEmpty()).isEqualTo(false);
        assertThat(user.get().getJmbg()).isEqualTo("1234567980123");
    }

    @Test
    void getUserByToken() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/user")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        User user = objectMapper.readValue(strResp, User.class);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("admin");
    }

    @Test
    void getUsers() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/users")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();

        List<User> users = objectMapper.readValue(strResp, new TypeReference<List<User>>() {});
        assertThat(users.size()).isNotEqualTo(0);
    }

    @Test
    void patchUserFromToken() throws Exception {
        CreateUserForm cuf = new CreateUserForm();
        String newJmbg = "111111111111111";
        cuf.setIme("Test");
        cuf.setPrezime("Testic");
        cuf.setEmail("test@raf.rs");
        cuf.setJmbg(newJmbg);
        cuf.setBrTelefon("0690000000");
        cuf.setPozicija("ROLE_ADMIN");
        cuf.setLimit(0.0);

        ResultActions resultActions = mockMvc.perform(patch("/api/user")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(cuf)))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        String strResp = mvcResult.getResponse().getContentAsString();
        User user = objectMapper.readValue(strResp, new TypeReference<User>() {});

        assertThat(user.getJmbg()).isEqualTo(newJmbg);
    }

    @Test
    void createAndDeleteAndReactivateUser() throws Exception {
        CreateUserForm cuf = new CreateUserForm();
        cuf.setIme("ToDelete");
        cuf.setPrezime("ToDeleteic");
        cuf.setEmail("to_delete@raf.rs");
        cuf.setJmbg("111111111111");
        cuf.setBrTelefon("0690000000");
        cuf.setPozicija("ROLE_AGENT");
        cuf.setLimit(0.0);
        cuf.setNeedsSupervisorPermission(false);

        mockMvc.perform(post("/api/user/create")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(cuf)))
                .andExpect(status().isOk());

        Optional<User> user = userRepository.findByUsername("todelete.todeleteic");
        assertThat(user).isPresent();
        String url = "/api/user/delete/" + user.get().getId();

        mockMvc.perform(delete(url)
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isOk());

        Optional<User> deleted_user = userRepository.findByEmail("to_delete@raf.rs");
        assertThat(deleted_user).isPresent();
        assertThat(deleted_user.get().isAktivan()).isEqualTo(false);

        url = "/api/user/enable/" + user.get().getId();
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(""))
                .andExpect(status().isOk());

        deleted_user = userRepository.findByEmail("to_delete@raf.rs");
        assertThat(deleted_user).isPresent();
        assertThat(deleted_user.get().isAktivan()).isEqualTo(true);
    }

    @Test
    void deleteInvalidId() throws Exception {
        mockMvc.perform(delete("/api/user/delete/-1")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editUserById() throws Exception {
        CreateUserForm cuf = new CreateUserForm();
        cuf.setIme("ToEdit");
        cuf.setPrezime("ToEditic");
        cuf.setEmail("to_edit@raf.rs");
        cuf.setJmbg("111111111111");
        cuf.setBrTelefon("0690000000");
        cuf.setPozicija("ROLE_AGENT");
        cuf.setLimit(0.0);
        cuf.setNeedsSupervisorPermission(false);

        mockMvc.perform(post("/api/user/create")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(cuf)))
                .andExpect(status().isOk());

        Optional<User> user = userRepository.findByUsername("toedit.toeditic");
        assertThat(user).isPresent();
        String url = "/api/user/edit/" + user.get().getId();
        String numberToChange = "068123123";

        CreateUserForm cufToEdit = new CreateUserForm();
        cufToEdit.setIme(user.get().getIme());
        cufToEdit.setPrezime(user.get().getPrezime());
        cufToEdit.setEmail(user.get().getEmail());
        cufToEdit.setJmbg(user.get().getJmbg());
        cufToEdit.setBrTelefon(numberToChange);
        cufToEdit.setPozicija(user.get().getRole().getName());
        cufToEdit.setLimit(user.get().getLimit());
        cufToEdit.setNeedsSupervisorPermission(user.get().isNeedsSupervisorPermission());

        mockMvc.perform(post(url)
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(cufToEdit)))
                .andExpect(status().isOk());

        Optional<User> changed_user = userRepository.findByUsername("toedit.toeditic");
        assertThat(changed_user).isPresent();
        assertThat(changed_user.get().getBrTelefon()).isEqualTo(numberToChange);
    }

    @Test
    void getUserIdByToken() throws Exception {
        String url = "/api/user/getId/" + token;

        ResultActions resultActions2 = mockMvc.perform(post(url)
                .contentType("application/json")
                .content(""))
                .andExpect(status().isOk());
        MvcResult mvcResult2 = resultActions2.andReturn();
        String id = mvcResult2.getResponse().getContentAsString().replace("\"", "");

        Optional<User> user = userRepository.findByUsername("admin");
        assertThat(user).isPresent();
        assertThat(Integer.parseInt(id)).isEqualTo(user.get().getId());

    }

    @Test
    void getUserIdByInvalidToken() throws Exception {
        String url = "/api/user/getId/invalidtoken";
        mockMvc.perform(post(url)
                .contentType("application/json")
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgentAndResetAgentLimitUsed() throws Exception {
        CreateUserForm cuf = new CreateUserForm();
        cuf.setIme("Agent");
        cuf.setPrezime("Agentic");
        cuf.setEmail("agent.agentic@raf.rs");
        cuf.setJmbg("1597538526851");
        cuf.setBrTelefon("058625896");
        cuf.setPozicija("ROLE_AGENT");
        cuf.setLimit(1000.0);
        cuf.setNeedsSupervisorPermission(false);

        mockMvc.perform(post("/api/user/create")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(cuf)))
                .andExpect(status().isOk());

        Optional<User> optUser = userRepository.findByUsername("agent.agentic");
        assertThat(optUser).isPresent();

        User user = optUser.get();
        user.setLimitUsed(500.0);

        userRepository.save(user);

        String url = "/api/limit-reset/" + user.getId();

        mockMvc.perform(patch(url)
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(""))
                .andExpect(status().isOk());

        Optional<User> changedOptUser = userRepository.findByUsername("agent.agentic");
        assertThat(changedOptUser).isPresent();

        User changedUser = changedOptUser.get();

        assertThat(changedUser.getLimitUsed()).isEqualTo(0.0);

    }
}
