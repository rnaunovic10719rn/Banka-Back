package rs.edu.raf.banka.racun.model.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.model.Valuta;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="company_id", nullable=false)
    @JsonIgnore
    private Company company;

    private String brojRacuna;
    private String banka;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private Valuta valuta;

    private boolean active;
    
}
