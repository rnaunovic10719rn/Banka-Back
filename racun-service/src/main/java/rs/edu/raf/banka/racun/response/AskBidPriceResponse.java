package rs.edu.raf.banka.racun.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskBidPriceResponse {

    private Long hartijaId;
    private Double ask;

}
