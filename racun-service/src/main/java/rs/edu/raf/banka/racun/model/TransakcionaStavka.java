package rs.edu.raf.banka.racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.racun.enums.TransakcionaStavkaType;

import javax.persistence.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransakcionaStavka {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    Ugovor ugovor;

    TransakcionaStavkaType type;

    Long hartijaId;

    HartijaOdVrednostiType hartijaType;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    Racun racun;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    Valuta valuta;

    Integer kolicina;

    Double cenaHartije;

}
