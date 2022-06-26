package rs.edu.raf.banka.berza;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.service.impl.UserService;
import rs.edu.raf.banka.berza.utils.HttpUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
