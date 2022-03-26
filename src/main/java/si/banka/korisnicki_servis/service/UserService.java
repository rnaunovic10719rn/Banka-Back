package si.banka.korisnicki_servis.service;

import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;

import java.util.List;

public interface UserService {
    User getUser(String username);
    User createUser(User user);
    User editUser(User user);
    void deleteUser(User user);
    List<User> getUsers();

    Role getRole(String role_name);
    Role saveRole(Role role);
    void setRoleToUser(String username, String role_name);
}