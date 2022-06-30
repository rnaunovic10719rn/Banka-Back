package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.KapitalType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KapitalHartijeDto {
    KapitalType kapitalType;
    double ukupno = 0.0;
}
