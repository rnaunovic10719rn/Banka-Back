package rs.edu.raf.banka.user_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String ime;
    private String prezime;
    private String email;
    private String jmbg;
    private String brTelefon;
    private String password;
    @Nullable
    private String otpSeecret;
    private boolean aktivan;
    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;
    private boolean requiresOtp;

    public boolean hasOTP()
    {
        return otpSeecret != null;
    }

    public User(String username, String ime, String prezime, String email, String jmbg, String brTelefon, String password, String otpSeecret, boolean aktivan, boolean requiresOtp, Role role) {
        this.username = username;
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.jmbg = jmbg;
        this.brTelefon = brTelefon;
        this.password = password;
        this.otpSeecret = otpSeecret;
        this.aktivan = aktivan;
        this.role = role;
        this.requiresOtp = requiresOtp;
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String otpSeecret){
        this.username = username;
        this.password = password;
        this.otpSeecret = otpSeecret;
    }
}
