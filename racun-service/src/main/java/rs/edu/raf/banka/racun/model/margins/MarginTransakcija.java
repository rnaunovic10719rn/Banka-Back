package rs.edu.raf.banka.racun.model.margins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.MarginTransakcijaType;
import rs.edu.raf.banka.racun.model.Racun;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarginTransakcija {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private MarginTransakcijaType tip;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    private Racun racun;

    private Date datumVreme;

    private Long orderId;
    private String username;
    private String opis;

    private KapitalType kapitalType = KapitalType.NOVAC;
    private Long haritjeOdVrednostiID;

    private Double iznos;
    private Double iznosKredita;
    private Double maintenanceMargin;

    private double kolicina;
    private double unitPrice;

}
