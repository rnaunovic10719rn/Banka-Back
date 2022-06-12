package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderStatus;
import rs.edu.raf.banka.berza.enums.OrderType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String username;

    @ManyToOne
    private Berza berza;

    @Enumerated(value = EnumType.STRING)
    private OrderAction orderAction;
    @Enumerated(value = EnumType.STRING)
    private OrderType orderType;
    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(value = EnumType.STRING)
    private HartijaOdVrednostiType hartijaOdVrednosti;
    private Long hartijaOdVrednostiId;
    private String hartijaOdVrednostiSymbol;

    private Integer kolicina;
    private Integer preostalaKolicina;
    private Integer backoff = -1;

    private Integer limitValue;
    private Integer stopValue;

    private Double predvidjenaCena;
    private Double provizija;

    private boolean AON;
    private boolean margin;

    // Koriste se samo pri izracunavanju, ne perzistujemo vrednosti
    @Transient
    private Double ask;
    @Transient
    private Double bid;

    @ColumnDefault("false")
    private Boolean done = false;

    private Date lastModified;

}
