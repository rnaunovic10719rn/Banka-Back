package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KapitalPoTipuHartijeDto {
    String oznakaHartije;
    String berza;
    Long kolicinaUVlasnistvu;
    Double cena;
    Double vrednost;
    Double kupljenoZa;
    Double profit;
}
