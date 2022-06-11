package rs.edu.raf.banka.racun.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KapitalStanjeDto {
    double ukupno;
    double novac;
    double forex;
    double future;
    double akcija;
}
