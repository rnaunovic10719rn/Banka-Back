package rs.edu.raf.banka.racun.model;

import lombok.*;
import rs.edu.raf.banka.racun.enums.RacunType;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Racun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID brojRacuna;
    private RacunType tipRacuna;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private Valuta valuta;

    private Double ulozenaSredstva;
    private Double pozajmljenaSredstva;

    private Double maintenanceMargin;
    @Column(name = "marginCall", columnDefinition = "boolean default false")
    private Boolean marginCall;
}
