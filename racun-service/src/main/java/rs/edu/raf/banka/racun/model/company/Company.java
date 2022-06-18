package rs.edu.raf.banka.racun.model.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;

    @Column(unique = true)
    private String maticniBroj;
    @Column(unique = true)
    private String pib;

    private String sifraDelatnosti;
    private String adresa;
    private String drzava;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private Set<CompanyContactPerson> companyContactPersons;
    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private Set<CompanyBankAccount> companyBankAccounts;

}
