package rs.edu.raf.banka.racun.model;

import lombok.*;
import rs.edu.raf.banka.racun.enums.HartijaOdVrednostiType;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MarzniRacun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "racun_id")
    private Racun racun;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private Valuta valuta;

    private long hartijaId;
    private HartijaOdVrednostiType hartijaOdVrednostiType;

    private Double ulozenaSredstva;
    private Double pozajmljenaSredstva;

    private Double maintenanceMargin;
    @Column(name = "marginCall", columnDefinition = "boolean default false")
    private Boolean marginCall;
}
