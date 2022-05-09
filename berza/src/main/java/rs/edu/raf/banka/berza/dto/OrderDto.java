package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.enums.OrderAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long orderId;
    private Long userId;
    private Long hartijaOdVrednostiId;
    private HartijaOdVrednostiType hartijaOdVrednosti;
    private String oznakaHartije;
    private Integer kolicina;
    private OrderAction orderAction;
    private double ukupnaCena;

}

