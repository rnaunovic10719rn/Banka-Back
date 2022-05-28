package rs.edu.raf.banka.user_service.service;

import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getUser(String username);
    Optional<User> getUserById(long id);
    User createUser(CreateUserForm createUserForm);
    User getUserByToken(String token);
    void createUserAdmin(User user);
    boolean hasEditPermissions(User user, String token);
    Long getUserId(String token);
    void editUser(User user, CreateUserForm createUserForm);
    User getUserByEmail(String email);
    boolean deleteUser(User user);
    boolean resetPassword(String email);
    boolean setNewPassword(String password, String token);
    List<User> getUsers();
    Role getRole(String roleName);
    Role saveRole(Role role);
    void setRoleToUser(String username, String roleName);
    void editOtpSeecret(User user, String optSeecret);
    boolean changePassword(String newPassword, User user);
    void resetLimitUsed(User user);
    void resetLimitUsedAllAgents();
    void changeLimit(User user, Double limit);
    User saveUser(User user);
}