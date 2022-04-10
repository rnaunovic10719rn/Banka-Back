package rs.edu.raf.banka.berza.requests;

import lombok.Data;

@Data
public class BuyHartijaOdVrednostiRequest {

    private Long idUser;
    private Long idHartijaOdVrednosti;
    private Integer kolicinaHartija;


}
