package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Valuta {
    private String kod_valute;
    private String naziv_valute;
    private String oznaka_valute;
    private String drzava;

}
