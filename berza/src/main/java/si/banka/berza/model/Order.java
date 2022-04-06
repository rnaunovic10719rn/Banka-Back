package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    //User
    private Long user_id;

    @OneToOne
    private HartijaOdVrednosti hartijaOdVrednosti;

    private Integer kolicina;
    private OrderAction action;

    @ElementCollection
    private List<OrderType> typeList;
}
