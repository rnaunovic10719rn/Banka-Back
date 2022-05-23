package racun.requests;

import lombok.Data;
import racun.model.Valuta;

@Data
public class UplataRequest {

    private long userid;
    private String brojRacuna;
    private String opis;
    private Valuta valuta;
    private long uplata;


}
