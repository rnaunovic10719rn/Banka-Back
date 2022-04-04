package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Akcije extends HartijaOdVrednosti{
    private long outstanding_shares;

    public double getPromenaProcenat() {
        // return (100 * (promena_iznos) / (cena - promena_iznos) );
        return 0;
    }

    public double getDollarVolume () {
        // return volume * cena;
        return 0;
    }

    public double getMarketCap() {
        // return outstanding_shares * cena;
        return 0;
    }

}
