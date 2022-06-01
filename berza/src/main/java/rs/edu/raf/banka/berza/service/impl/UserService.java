package rs.edu.raf.banka.berza.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.UserDto;
import rs.edu.raf.banka.berza.utils.HttpUtils;

@Service
public class UserService {

    public UserDto getUserByToken(String token) {
        ResponseEntity<UserDto> response = HttpUtils.getUser(HttpUtils.USER_SERVICE_URL, token);
        return response.getBody();
    }

    public String getUserRoleByToken(String token) {
        try {
            DecodedJWT decodedToken = decodeToken(token);
            return decodedToken.getSubject().split(",")[1];
        } catch (JWTVerificationException e) {
            // TODO find a better exception for this case
            throw new UsernameNotFoundException("Token is invalid");
        }
    }

    private DecodedJWT decodeToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }

        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier  = JWT.require(algorithm).build();
        DecodedJWT  decoded   = verifier.verify(token);
        return decoded;
    }
}
