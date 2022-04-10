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

    private String oznaka_hartije;
    private String opis_hartije;

    @ManyToOne
    private Berza berza;

    private Date last_updated;
    private Double cena;
    private Double ask;
    private Double bid;
    private Double promena_iznos;
    private Long volume;

    @ElementCollection
    private List<String> istorijski_podaci;
}
