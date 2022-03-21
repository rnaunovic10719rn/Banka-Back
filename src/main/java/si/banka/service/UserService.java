package si.banka.service;

import si.banka.model.Role;
import si.banka.model.User;

import java.util.List;

public interface UserService {
    User getUser(String username);
    List<User> getUsers();
    User saveUser(User user);
    Role saveRole(Role role);
    void setRoleToUser(String username, String role_name);
}