package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Test
    void testGetUsernameByToken(){
        assertEquals(userService.getUsernameByToken(initValidJWT()),"dummyName");
    }

    @Test
    void testGetUsernameByTokenInvalid(){
        assertThrows(UsernameNotFoundException.class, () -> userService.getUsernameByToken("s"+initValidJWT() ));
    }

    @Test
    void testGetRoleByToken(){
        assertEquals(userService.getRoleByToken(initValidJWT()),"ADMIN_ROLE");
    }

    @Test
    void testGetRoleByTokenInvalid(){
        assertThrows(UsernameNotFoundException.class, () -> userService.getRoleByToken("s"+initValidJWT() ));
    }

    @Test
    void testGetPermissionsByToken(){
        assertEquals(Arrays.stream(userService.getPermissionsByToken(initValidJWT())).toList(), Arrays.stream(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}).toList());
    }

    @Test
    void testGetPermissionByTokenInvalid(){
        assertThrows(UsernameNotFoundException.class, () -> userService.getPermissionsByToken("s"+initValidJWT() ));
    }


    String initValidJWT() {
        return JWT.create()
                .withSubject("dummyName" + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    String initInvalidJWT() {
        return JWT.create()
                .withSubject("dummyName" + ",ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"X_LIST_USERS", "DUMMY_FAKE_PERMISSION"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

}


