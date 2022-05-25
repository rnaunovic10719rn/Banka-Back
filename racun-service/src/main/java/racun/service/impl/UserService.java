package racun.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String getUserByToken(String token) {
        try {
            DecodedJWT decodedToken = decodeToken(token);

            return decodedToken.getSubject();
        } catch (JWTVerificationException e) {
            // TODO find a better exception for this case
            throw new UsernameNotFoundException("Token is invalid");
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
