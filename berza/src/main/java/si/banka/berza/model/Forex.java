package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Forex extends HartijaOdVrednosti{
    private Valuta base_currency;
    private Valuta quote_currency;
    private long contract_size;
    private double lot_size;

    public double getNominalnaVrednost () {
        return lot_size * super.getCena();
    }

}
