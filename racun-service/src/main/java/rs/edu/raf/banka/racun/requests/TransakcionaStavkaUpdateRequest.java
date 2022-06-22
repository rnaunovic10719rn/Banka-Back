package rs.edu.raf.banka.racun.requests;

import lombok.Data;
import rs.edu.raf.banka.racun.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.racun.enums.TransakcionaStavkaType;

@Data
public class TransakcionaStavkaUpdateRequest
{
    Long stavkaId;

    TransakcionaStavkaType type;

    Long hartijaId;

    HartijaOdVrednostiType hartijaType;

    Long racunId;

    String valuta;

    Integer kolicina;

    Double cenaHartije;
}
