package rs.edu.raf.banka.racun.dto;

import lombok.*;
import rs.edu.raf.banka.racun.enums.KapitalType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KapitalHartijeDto {
    KapitalType kapitalType;
    double ukupno = 0.0;
}
