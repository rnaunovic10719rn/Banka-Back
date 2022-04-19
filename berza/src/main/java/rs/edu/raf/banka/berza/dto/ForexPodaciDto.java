package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForexPodaciDto {

    String fromCurrency;
    String toCurrency;
    String time;
    Double exchangeRate;
    Double bid;
    Double ask;
    Long id;

}
