package si.banka.berza.requests;

import lombok.Data;

@Data
public class OrderRequest {

    private Long berza_id;
    private Long user_id;
    private Long hartija_od_vrednosti_id;
    private String hartija_od_vrednosti_tip;
    private Integer kolicina;
    private String akcija;
    private Integer limitValue;
    private Integer stopValue;
    private boolean allOrNoneFlag;
    private boolean marginFlag;

}
