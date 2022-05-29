package racun.requests;

import lombok.Data;

@Data
public class RezervacijaRequest {

    private String racun;
    private String valuta;
    private String opis;
    private float isplata;
    private float RezervacijaKoristi;
    private long hartijeOdVrednostiID;
}
