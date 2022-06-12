package rs.edu.raf.banka.berza.requests;

import lombok.Data;

@Data
public class OrderRequest {

    private String symbol;
    private String hartijaOdVrednostiTip;
    private Integer kolicina;
    private String akcija;
    private Integer limitValue;
    private Integer stopValue;
    private boolean allOrNoneFlag;
    private boolean marginFlag;

}
