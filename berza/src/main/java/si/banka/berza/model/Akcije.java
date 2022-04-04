package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Akcije extends HartijaOdVrednosti{
    private long outstanding_shares;

    public double getPromenaProcenat() {
        return (100 * (super.getPromena_iznos()) / (super.getCena() - super.getPromena_iznos()) );
    }

    public double getDollarVolume () {
        return super.getVolume() * super.getCena();
    }

    public double getMarketCap() {
        return outstanding_shares * super.getCena();
    }

}
