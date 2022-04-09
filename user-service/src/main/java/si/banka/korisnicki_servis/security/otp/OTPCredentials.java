package si.banka.korisnicki_servis.security.otp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OTPCredentials {

    public String password;
    public String otp;
}
