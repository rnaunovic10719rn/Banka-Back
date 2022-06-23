package rs.edu.raf.banka.racun.requests;

import lombok.Data;
import rs.edu.raf.banka.racun.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.enums.TransakcionaStavkaType;

@Data
public class TransakcionaStavkaCreateRequest
{
    Long ugovorId;

    TransakcionaStavkaType type;

    Long hartijaId;

    HartijaOdVrednostiType hartijaType;

    RacunType racunType;

    String valuta;

    Integer kolicina;

    Double cenaHartije;
}
