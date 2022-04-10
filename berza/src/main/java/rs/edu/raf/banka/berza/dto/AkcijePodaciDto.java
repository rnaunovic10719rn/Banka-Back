package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AkcijePodaciDto {
    String ticker;
    String opisHartije;
    String lastUpdate;
    Double open;
    Double high;
    Double low;
    Double close;
    Long volume;
    Long outstandingShares;
}
