package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Forex extends HartijaOdVrednosti{

    @ManyToOne
    private Valuta baseCurrency;

    @ManyToOne
    private Valuta quoteCurrency;

    private Long contractSize;
    private Double lotSize;

}
