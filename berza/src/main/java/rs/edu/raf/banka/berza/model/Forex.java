package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Forex extends HartijaOdVrednosti{

    @ManyToOne
    private Valuta base_currency;

    @ManyToOne
    private Valuta quote_currency;

    private Long contract_size;
    private Double lot_size;

    public double getNominalnaVrednost () {
        return lot_size * super.getCena();
    }
}
