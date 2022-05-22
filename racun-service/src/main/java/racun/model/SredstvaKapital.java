package racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SredstvaKapital {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    private Racun racun;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private  Valuta valuta;

    private Long haritjeOdVrednostiID;

    private double ukupno;
    private double rezervisano;
    private double raspolozivo;

    @Version
    private Integer version = 0;
}
