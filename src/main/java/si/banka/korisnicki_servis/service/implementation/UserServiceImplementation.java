package si.banka.korisnicki_servis.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.repository.RoleRepository;
import si.banka.korisnicki_servis.repository.UserRepository;
import si.banka.korisnicki_servis.service.UserService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImplementation implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            log.error("User {} not found in database", username);
            throw new UsernameNotFoundException("User not found in database");
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRole().getPermissions().forEach(permission -> {
            authorities.add(new SimpleGrantedAuthority(permission));
        });

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    public User getUser(String username) {
        log.info("Showing user {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getUsers() {
        log.info("Showing list of users");
        return userRepository.findAll();
    }

    @Override
    public Role getRole(String role_name) {
        return roleRepository.findByName(role_name);
    }

    @Override
    public void deleteUser(User user) {
        if(user.getRole().getName().equalsIgnoreCase("ROLE_GL_ADMIN"))
            return;

        log.info("Deleting user {} from database", user.getUsername());
        userRepository.delete(user);
    }

    @Override
    public User createUser(User user) {
        log.info("Saving new user {} to the database", user.getUsername());
        String hash_pw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash_pw);
        return userRepository.save(user);
    }

    @Override
    public User editUser(User user) {
        return null;
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }

    @Override
    public void setRoleToUser(String username, String role_name) {
        log.info("Adding role {} to user {} to the database", role_name, username);
        User user = userRepository.findByUsername(username);
        Role role = roleRepository.findByName(role_name);
        user.setRole(role);
    }

}
