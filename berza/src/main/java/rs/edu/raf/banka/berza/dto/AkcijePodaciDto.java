package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AkcijePodaciDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -7891725050066298461L;

    String ticker;
    String opisHartije;
    String time;
    Double open;
    Double high;
    Double low;
    Double price;
    Long volume;
    Double previousClose;
    Double change;
    Double changePercent;
    Long outstandingShares;
    Long berzaId;
    Long id;

    // Calculated
    Double priceVolume;
    Double marketCap;

    public Double getPriceVolume() {
        return volume * price;
    }

    public Double getMarketCap() {
        return outstandingShares * price;
    }

}
