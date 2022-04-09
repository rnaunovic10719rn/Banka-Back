package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.banka.berza.enums.HartijaOdVrednostiType;
import si.banka.berza.enums.OrderAction;
import si.banka.berza.enums.OrderType;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserAccount userAccount;

    private Long hartijaOdVrednostiId;

    @Enumerated(value = EnumType.STRING)
    private HartijaOdVrednostiType hartijaOdVrednosti;

    private Integer kolicina;

    @Enumerated(value = EnumType.STRING)
    private OrderAction orderAction;
    private Double ukupnaCena;
    private Double provizija;
    private Integer limitValue;
    private Integer stopValue;

    @Enumerated(value = EnumType.STRING)
    private OrderType orderType;
    private boolean AON;
    private boolean margin;
}
