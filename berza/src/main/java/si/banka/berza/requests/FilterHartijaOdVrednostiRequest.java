package si.banka.berza.requests;

import lombok.Data;

@Data
public class FilterHartijaOdVrednostiRequest {

    private String berzaPrefix;
    private Double priceLowBound;
    private Double priceUpperBound;
    private Double askLowBound;
    private Double askUpperBound;
    private Double bidLowBound;
    private Double bidUpperBound;
    private Long volumeLowBound;
    private Long volumeUpperBound;

}
