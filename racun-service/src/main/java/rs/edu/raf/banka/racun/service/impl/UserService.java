package rs.edu.raf.banka.racun.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.UserDto;
import rs.edu.raf.banka.racun.utils.HttpUtils;

@Service
public class UserService {

    @Value("${racun.user-service-url2}")
    private String USER_SERVICE_URL2;

    public UserDto getUserByToken(String token) {
        ResponseEntity<UserDto> response = HttpUtils.getUser(USER_SERVICE_URL2, token);
        UserDto userDto = response.getBody();
        if(userDto == null) {
            return null;
        }
        userDto.setRoleName(getRoleByToken(token));
        return userDto;
    }

    public String getUsernameByToken(String token) {
        try {
            DecodedJWT decodedToken = decodeToken(token);

            return decodedToken.getSubject().split(",")[0];
        } catch (JWTVerificationException e) {
            // TODO find a better exception for this case
            throw new UsernameNotFoundException("bad credentials");
        }
    }

    public String getRoleByToken(String token) {
        try {
            DecodedJWT decodedToken = decodeToken(token);

            return decodedToken.getSubject().split(",")[1];
        } catch (JWTVerificationException e) {
            // TODO find a better exception for this case
            throw new UsernameNotFoundException("bad credentials");
        }
    }

    public String[] getPermissionsByToken(String token) {
        try {
            DecodedJWT decodedToken = decodeToken(token);

            return decodedToken.getClaim("permissions").asArray(String.class);
        } catch (JWTVerificationException e) {
            // TODO find a better exception for this case
            throw new UsernameNotFoundException("bad credentials");
        }
    }

    private DecodedJWT decodeToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }

        Algorithm   algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier  = JWT.require(algorithm).build();
        DecodedJWT  decoded   = verifier.verify(token);
        return decoded;
    }
}
