package si.banka.korisnicki_servis.security.otp;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import si.banka.korisnicki_servis.security.OTPUtilities;
import si.banka.korisnicki_servis.service.implementation.UserServiceImplementation;

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
            return authentication;

        // Provera OTP Koda
        var user =_userServiceImplementation.getUser(otpToken.getUsername());
        var otpSeecret = user.getOtpSeecret();
        var otp = OTPUtilities.getTOTPCode(otpSeecret);
        if(!otp.equals(otpToken.getOtp()))
            return authentication;

        var passwordToken = new UsernamePasswordAuthenticationToken(otpToken.getUsername(), otpToken.getPassword());
        return _baseAuthenticationManager.authenticate(passwordToken);
    }
}
