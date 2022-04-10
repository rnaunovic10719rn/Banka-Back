package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.berza.enums.OrderAction;
import rs.edu.raf.banka.berza.enums.OrderType;

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

    private Date vremeTranskacije;
    private Integer kolicina;
    private Double cena;

    @ManyToOne
    private Order order;

}
