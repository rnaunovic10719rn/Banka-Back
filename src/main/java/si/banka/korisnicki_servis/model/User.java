package si.banka.korisnicki_servis.model;

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
    private String br_telefon;
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

    public User(String username, String ime, String prezime, String email, String jmbg, String br_telefon, String password, boolean aktivan, Role role) {
        this.username = username;
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.jmbg = jmbg;
        this.br_telefon = br_telefon;
        this.password = password;
        this.aktivan = aktivan;
        this.role = role;
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
}
