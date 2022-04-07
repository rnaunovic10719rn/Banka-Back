package si.banka.berza.requests;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    private Long user_id;
    private Long hartija_od_vrednosti_id;
    private String hartija_od_vrednosti_tip;
    private Integer kolicina;
    private String akcija;
    private List<String> order_tip;

}
