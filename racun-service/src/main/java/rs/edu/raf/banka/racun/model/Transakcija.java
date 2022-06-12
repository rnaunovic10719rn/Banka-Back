package rs.edu.raf.banka.racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.KapitalType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transakcija {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    private Racun racun;

    private Date datumVreme;

    private Long orderId;
    private String username;
    private String opis;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private Valuta valuta;

    private KapitalType kapitalType = KapitalType.NOVAC;
    private Long haritjeOdVrednostiID;

    private double uplata;
    private double isplata;
    private double rezervisano;
    private double rezervisanoKoristi;

}
