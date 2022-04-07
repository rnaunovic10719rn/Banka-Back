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
    private Long id_order;

    @ManyToOne
    private UserAccount user;

    private Long hartijaOdVrednosti_id;

    @Enumerated(value = EnumType.STRING)
    private HartijaOdVrednostiType hartijaOdVrednosti;

    private Integer kolicina;

    @Enumerated(value = EnumType.STRING)
    private OrderAction action;
    private Double ukupnaCena;
    private Double provizija;

    @ElementCollection
    @Enumerated(value = EnumType.STRING)
    private List<OrderType> typeList;


//    public void izracunajUkupnuCenu(){
//        ukupnaCena = kolicina * hartijaOdVrednosti.getCena();
//    }
}
