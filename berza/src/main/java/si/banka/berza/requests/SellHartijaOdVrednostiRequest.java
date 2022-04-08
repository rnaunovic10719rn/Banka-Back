package si.banka.berza.requests;

import lombok.Data;

@Data
public class SellHartijaOdVrednostiRequest {

    private Long idUser;
    private Long idHartijaOdVrednosti;
    private Integer kolicinaHartijaOdVrednosti;
}
