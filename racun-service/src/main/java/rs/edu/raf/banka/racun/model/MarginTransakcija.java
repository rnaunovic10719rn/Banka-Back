package rs.edu.raf.banka.racun.model;

import lombok.*;
import rs.edu.raf.banka.racun.enums.MarginTransakcijaType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MarginTransakcija {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date datumTransakcije;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    private Racun racun;

    private Long orderId;
    private Long userId;

    private String opisTransakcije;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private Valuta valuta;

    private MarginTransakcijaType tipTransakcije;

    private Double ulozenaSredstva;
    private Double pozajmljenaSredstva;

    private Double maintenanceMargin;

    private Double iznosKamate;
}
