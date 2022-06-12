package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.berza.model.Berza;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskBidPriceDto {

    private Long hartijaId;
    private Berza berza;
    private Double ask;
    private Double bid;

}
