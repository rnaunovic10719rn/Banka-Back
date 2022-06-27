package rs.edu.raf.banka.berza.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

//@Entity
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass // samo za kod, nema veze sa bazon
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class HartijaOdVrednosti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oznakaHartije;
    private String opisHartije;

    @ManyToOne
    private Berza berza;

    private Date lastUpdated;
    private Double cena = 0.0;
    private Double ask = 0.0;
    private Double bid = 0.0;
    private Double promenaIznos = 0.0;
    private Long volume = 0L;

    @ElementCollection
    private List<String> istorijskiPodaci;

    private Boolean custom = false;
}
