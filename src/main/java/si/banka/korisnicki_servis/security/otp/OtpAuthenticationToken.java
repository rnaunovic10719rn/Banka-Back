package si.banka.korisnicki_servis.security.otp;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class OtpAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 560L;
    private final Object principal;
    private Object credentials;

    public OtpAuthenticationToken(String username, String password, @Nullable String otp) {
        super((Collection)null);
        Assert.notNull(username, "Username cannot be null");
        Assert.notNull(password, "Password cannot be null");
        this.principal = username;
        this.credentials = new OTPCredentials(password, otp);
        this.setAuthenticated(false);
    }

    public OtpAuthenticationToken(String username, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(username, "Username cannot be null");
        this.principal = username;
        this.credentials = null;
        super.setAuthenticated(true);
    }

    @Nullable
    public String getPassword() {
        if(this.credentials == null || !(this.credentials instanceof OTPCredentials creds))
            return null;
        return creds.getPassword();
    }

    @Nullable
    public String getOtp() {
        if(this.credentials == null || !(this.credentials instanceof OTPCredentials creds))
            return null;
        return creds.getOtp();
    }

    @Nullable
    public String getUsername() {
        if(this.principal == null || !(this.principal instanceof String username))
            return null;
        return username;
    }

    @Override
    public Object getCredentials() { return credentials; }

    @Nullable
    @Override
    public Object getPrincipal() { return principal; }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}