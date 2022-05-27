package racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private long order_id;
    private String username;
    private String opis;

    @ManyToOne
    @JoinColumn(name = "valuta_id")
    private Valuta valuta;

    private long uplata;
    private long isplata;
    private long rezervisano;
    private long rezervisanoKoristi;

}
