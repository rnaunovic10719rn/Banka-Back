package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.model.Valuta;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SredstvaKapitalDto {

    private String kodValute;
    private double ukupno;
    private double rezervisano;
    private double raspolozivo;
}
