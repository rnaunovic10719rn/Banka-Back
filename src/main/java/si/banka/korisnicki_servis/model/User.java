package si.banka.korisnicki_servis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String ime;
    private String prezime;
    private String email;
    private String jmbg;
    private String br_telefon;
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;
}
