package rs.edu.raf.banka.racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.KapitalType;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SredstvaKapital {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    private Racun racun;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private  Valuta valuta;

    private KapitalType kapitalType = KapitalType.NOVAC;
    private Long haritjeOdVrednostiID;

    private double ukupno;
    private double rezervisano;
    private double raspolozivo;

    @Version
    private Integer version = 0;

    private Double ulozenaSredstva;
    private Double pozajmljenaSredstva;

    private Double maintenanceMargin;
    private Boolean marginCall = false;
}
