package rs.edu.raf.banka.user_service.service.implementation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.user_service.controller.response_forms.CreateUserForm;
import rs.edu.raf.banka.user_service.mail.PasswordResetToken;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.repository.PasswordTokenRepository;
import rs.edu.raf.banka.user_service.repository.RoleRepository;
import rs.edu.raf.banka.user_service.repository.UserRepository;
import rs.edu.raf.banka.user_service.service.UserService;
import org.springframework.jms.core.JmsTemplate;
import javax.jms.Queue;
import org.springframework.messaging.MessagingException;

import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@Slf4j
public class UserServiceImplementation implements UserService, UserDetailsService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final RoleRepository roleRepository;
    @Autowired
    private final PasswordTokenRepository passwordTokenRepository;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    Queue mailQueue;

    private String bearer = "Bearer ";
    private String secret = "secret";
    private String errMessage = "Bad credentials";
    private SecureRandom rnd = new SecureRandom();

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, RoleRepository roleRepository, PasswordTokenRepository passwordTokenRepository){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordTokenRepository = passwordTokenRepository;
    }

    @Override
    public void resetLimitUsed(User user) {
        user.setLimitUsed(0.0);
        userRepository.save(user);
    }

    @Override
    public void resetLimitUsedAllAgents() {
        List<User> users = userRepository.findAll();
        for(User u: users){
            u.setLimitUsed(0.0);
        }
        userRepository.saveAll(users);
    }

    @Override
    public void changeLimit(User user, Double limitDelta) {
        if(!user.getRole().getName().equals("ROLE_AGENT")) {
            return;
        }
        if(user.getLimit() == null) {
            user.setLimit(0.0);
        }
        if(user.getLimitUsed() == null) {
            user.setLimitUsed(0.0);
        }
        Double newLimitUsed = user.getLimitUsed() + limitDelta;
        user.setLimitUsed(newLimitUsed);
        userRepository.save(user);
        return;
    }

    @Override
    public User saveUser(User user) {
        log.info("Saving new user {} to the database", user.getUsername());
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()){
            if(!(user.get().isAktivan())){
                log.error("User {} not found in database", username);
                throw new UsernameNotFoundException(errMessage);
            }
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            user.get().getRole().getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission)));

            return new org.springframework.security.core.userdetails.User(user.get().getUsername() + "," + user.get().getRole().getName(), user.get().getPassword(), authorities);

        }else{
            throw new UsernameNotFoundException(errMessage);
        }
    }

    @Override
    public User getUser(String username) {
        log.info("Showing user {}", username);
        if(userRepository.findByUsername(username).isPresent()){
            Optional<User> user = userRepository.findByUsername(username);
            if(user.isPresent()){
                return user.get();
            }
        }
        return null;
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
    public Role getRole(String roleName) {
        return roleRepository.findByName(roleName);
    }

    @Override
    public boolean deleteUser(User user){
        if(user.getRole().getName().equalsIgnoreCase("ROLE_GL_ADMIN")){
            return false;
        }

        log.info("Setting user inactive {} in database", user.getUsername());
        user.setAktivan(false);
        return true;
    }

    @Override
    public void enableUser(User user){
        log.info("Setting user ACTIVE {} in database", user.getUsername());
        user.setAktivan(true);
    }

    @Override
    public User createUser(CreateUserForm createUserForm) {
        String username = createUserForm.getIme().toLowerCase()+ "." + createUserForm.getPrezime().toLowerCase();

        if(this.getUser(username) instanceof User){
            username = username + (rnd.nextInt() * (100)) + 1;
        }

        String password = createUserForm.getIme() + "Test123";

        User user = new User(username, createUserForm.getIme(),
                            createUserForm.getPrezime(), createUserForm.getEmail(),
                            createUserForm.getJmbg(), createUserForm.getBrTelefon(),
                            password, null, true, false, this.getRole(createUserForm.getPozicija()));
        if(createUserForm.getPozicija().equals("ROLE_AGENT")){
            user.setLimit(createUserForm.getLimit());
            user.setLimitUsed(0.0);
            user.setNeedsSupervisorPermission(createUserForm.isNeedsSupervisorPermission());
        }

        log.info("Saving new user {} to the database", user.getUsername());
        String hashPW = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashPW);
        return userRepository.save(user);
    }

    @Override
    public User getUserByToken(String token) {
        try {
            DecodedJWT decodedToken = decodeToken(token);
            String username = decodedToken.getSubject().split(",")[0];
            var user = userRepository.findByUsername(username);

            if (!user.isPresent() || !(user.get().isAktivan())) {
                log.error("User {} not found in database", username);
                throw new BadCredentialsException(errMessage);
            }else{
                return user.get();
            }
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Token is invalid");
        }
    }

    @Override
    public void createUserAdmin(User user){
        log.info("Saving admin to the database");
        String hashPW = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashPW);
        user.setAktivan(true);
        userRepository.save(user);
    }

    @Override
    public boolean hasEditPermissions(User user, String token) {
        if(token.startsWith(bearer))
            token = token.substring(bearer.length());
        //Cupamo username i permisije iz tokena
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String usernameFromJWT = decodedJWT.getSubject().split(",")[0];

        //glavni admin moze biti editovan samo ako je ulogovan kao glavni amdin
        if(user.getUsername().equals("admin"))
        {
            return usernameFromJWT.equalsIgnoreCase(user.getUsername());
        }

        String[] permissionsFromJWT = decodedJWT.getClaim("permissions").asArray(String.class);

        if(user.getUsername().equalsIgnoreCase(usernameFromJWT) && hasEditPermission(permissionsFromJWT, Permissions.MY_EDIT))
            return true;
        return hasEditPermission(permissionsFromJWT, Permissions.EDIT_USER);
    }

    @Override
    public Long getUserId(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String usernameFromJWT = decodedJWT.getSubject().split(",")[0];
        var user = this.getUser(usernameFromJWT);
        if(user == null)
            return null;
        return user.getId();
    }

    @Override
    public void editUser(User user, CreateUserForm newUser) {
        user.setIme(newUser.getIme());
        user.setPrezime(newUser.getPrezime());
        user.setEmail(newUser.getEmail());
        user.setJmbg(newUser.getJmbg());
        user.setBrTelefon(newUser.getBrTelefon());
        user.setRole(getRole(newUser.getPozicija()));
        user.setNeedsSupervisorPermission(newUser.isNeedsSupervisorPermission());
        userRepository.save(user);
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }

    @Override
    public void setRoleToUser(String username, String roleName) {
        log.info("Adding role {} to user {} to the database", roleName, username);
        var user = userRepository.findByUsername(username);
        if(user.isPresent()){
            Role role = roleRepository.findByName(roleName);
            user.get().setRole(role);
        }
    }

    @Override
    public void editOtpSeecret(User user, @Nullable String optSeecret) {
        user.setOtpSeecret(optSeecret);
        userRepository.save(user);
    }

    public boolean hasEditPermission(String[] permissions,Permissions permission){
        return Arrays.stream(permissions).anyMatch(pm -> pm.equalsIgnoreCase(String.valueOf(permission)));
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }

    @Override
    public User getUserByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()){
            return user.get();
        }
        return null;
    }

    @Override
    public boolean resetPassword(String email){
        User user = this.getUserByEmail(email);
        if(user == null){
            return false;
        }

        String token = UUID.randomUUID().toString();
        this.createPasswordResetTokenForUser(user, token);
        this.sendMail(email, token);

        return true;
    }

    public boolean changePassword(String password, User user){
        //Checking password
        String regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()) return false;

        String hashPW = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashPW);
        userRepository.save(user);
        return true;
    }

    public void sendMail(String email, String token) throws MessagingException {
        String to = email;
        String url = "http://localhost:3000/changepassword/" + token;
        String link ="<a href='" + url + "'>" + url + "</a>";
        String subject = "Password reset";
        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<h1>Link ka resetovanju passworda</h1>\n" +
                "\n" +
                "<p>"+ link +"</p>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        jmsTemplate.convertAndSend(mailQueue , to + "###" + subject + "###" + content);
    }

    @Override
    public boolean setNewPassword(String password, String token) {
        if (token.startsWith(bearer)) {
            token = token.substring(bearer.length());
        }else{
            return false;
        }

        PasswordResetToken prt = this.passwordTokenRepository.findByToken(token);
        if(prt == null){
            return false;
        }

        //Checking password
        String regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()) throw new BadCredentialsException("Password: must have 8 characters,one uppercase and one digit minimum");

        User user = prt.getUser();
        String hashPW = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashPW);

        this.userRepository.save(user);
        return true;
    }

    private DecodedJWT decodeToken(String token) throws JWTVerificationException {
        if (token.startsWith(bearer)) {
            token = token.substring(bearer.length());
        }

        Algorithm   algorithm = Algorithm.HMAC256(secret.getBytes());
        JWTVerifier verifier  = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
}
