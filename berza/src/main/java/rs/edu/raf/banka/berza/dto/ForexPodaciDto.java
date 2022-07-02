package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Promeniti i kopiju u racun-service ako se desi promena
public class ForexPodaciDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -5427705800284041407L;

    String fromCurrency;
    String toCurrency;
    String time;
    Double exchangeRate;
    Double bid;
    Double ask;
    Long id;

}
