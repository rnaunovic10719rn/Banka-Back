package si.banka.korisnicki_servis.service;

import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getUser(String username);
    Optional<User> getUserById(long id);
    User createUser(CreateUserForm createUserForm);
    void createUserAdmin(User user);
    User editUser(User user, String token);
    void deleteUser(User user);

    List<User> getUsers();
    Role getRole(String role_name);
    Role saveRole(Role role);
    void setRoleToUser(String username, String role_name);
    String setUserOtp(String username, String seecret);
    void clearUserOtp(String username);
}