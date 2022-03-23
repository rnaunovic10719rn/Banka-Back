package si.banka.korisnicki_servis.service;

import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;

import java.util.List;

public interface UserService {
    User getUser(String username);
    List<User> getUsers();
    User saveUser(User user);
    Role saveRole(Role role);
    void setRoleToUser(String username, String role_name);
}