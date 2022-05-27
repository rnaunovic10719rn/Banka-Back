package racun.requests;

import lombok.Data;
import racun.model.Valuta;

@Data
public class UplataRequest {

    private String brojRacuna;
    private String opis;
    private int valuta_id;
    private long uplata;


}
