package rs.edu.raf.banka.user_service.security.otp;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import rs.edu.raf.banka.user_service.security.OTPUtilities;
import rs.edu.raf.banka.user_service.service.implementation.UserServiceImplementation;

public class OtpAuthenticationManager implements AuthenticationManager {

    private AuthenticationManager _baseAuthenticationManager;
    private UserServiceImplementation _userServiceImplementation;

    public OtpAuthenticationManager(UserServiceImplementation userServiceImplementation, AuthenticationManager baseAuthenticationManager)
    {
        _baseAuthenticationManager = baseAuthenticationManager;
        _userServiceImplementation = userServiceImplementation;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if(!(authentication instanceof OtpAuthenticationToken otpToken))
            throw new AuthenticationCredentialsNotFoundException("");
        // Provera OTP Koda
        var user =_userServiceImplementation.getUser(otpToken.getUsername());
        if(user.hasOTP()){
            var otpSeecret = user.getOtpSeecret();
            var sentOtp = otpToken.getOtp();
            if(!OTPUtilities.validate(otpSeecret, sentOtp))
                throw new AuthenticationCredentialsNotFoundException("");
        }

        var passwordToken = new UsernamePasswordAuthenticationToken(otpToken.getUsername(), otpToken.getPassword());
        return _baseAuthenticationManager.authenticate(passwordToken);
    }
}
