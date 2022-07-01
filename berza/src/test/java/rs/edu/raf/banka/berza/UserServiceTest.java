package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import rs.edu.raf.banka.berza.service.impl.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;


    @Test
    void testGetUserRoleByToken(){
        String token = "Bearer s";
        assertThrows(UsernameNotFoundException.class,() -> userService.getUserRoleByToken(token));
    }
}
