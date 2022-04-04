package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Forex extends HartijaOdVrednosti{
    private Valuta base_currency; //???? nisam siguran za tip
    private Valuta quote_currency; //???? nisam siguran za tip
    private long contract_size;
}
