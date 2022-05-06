package rs.edu.raf.banka.berza.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class IstorijaInflacije {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Valuta valuta;

    @Column
    private String year;

    @Column
    private Double inflationRate;

}
