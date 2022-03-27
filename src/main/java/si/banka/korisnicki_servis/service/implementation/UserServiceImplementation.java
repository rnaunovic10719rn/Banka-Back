package si.banka.korisnicki_servis.service.implementation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.model.Permissions;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.repository.RoleRepository;
import si.banka.korisnicki_servis.repository.UserRepository;
import si.banka.korisnicki_servis.service.UserService;

import javax.transaction.Transactional;
import java.util.*;

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
        if(user == null || !(user.isAktivan())){
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
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
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

        log.info("Setting user inactive {} in database", user.getUsername());
        user.setAktivan(false);
    }

    @Override
    public User createUser(CreateUserForm createUserForm) {
        String username = createUserForm.getIme().toLowerCase()+ "." + createUserForm.getPrezime().toLowerCase();
        if(this.getUser(username) instanceof User){
            username = username + (int)(Math.random() * (100)) + 1;
        }
        String password = createUserForm.getIme() + "Test123";
        User user = new User(username, createUserForm.getIme(),
                            createUserForm.getPrezime(), createUserForm.getEmail(),
                            createUserForm.getJmbg(), createUserForm.getBr_telefon(),
                            password, true, this.getRole(createUserForm.getPozicija()));
        log.info("Saving new user {} to the database", user.getUsername());
        String hash_pw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash_pw);
        return userRepository.save(user);
    }

    @Override
    public void createUserAdmin(User user){
        log.info("Saving admin to the database");
        String hash_pw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash_pw);
        user.setAktivan(true);
        userRepository.save(user);
    }

    @Override
    public User editUser(User user, String token) {
        //Cupamo username i permisije iz tokena
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String usernameFromJWT = decodedJWT.getSubject();
        String[] permissionsFromJWT = decodedJWT.getClaim("permissions").asArray(String.class);
        //Ako si to ti, edituj se || Ako nisi mozda je admin || u suprotnom neko hoce da edituje drugog a nije admin
        if(user.getUsername().equalsIgnoreCase(usernameFromJWT) && Arrays.stream(permissionsFromJWT).anyMatch(permission -> permission.equalsIgnoreCase(String.valueOf(Permissions.MY_EDIT)))) {
            //edit logika set na usera?
        }else if(Arrays.stream(permissionsFromJWT).anyMatch(permission -> permission.equalsIgnoreCase(String.valueOf(Permissions.EDIT_USER)))){
            //edit logika
        }

        return user;
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
