package rs.edu.raf.banka.racun.model.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="company_id", nullable=false)
    @JsonIgnore
    private Company company;

    private String ime;
    private String prezime;
    private String email;
    private String brojTelefona;
    private String pozicija;
    private String napomena;

}
