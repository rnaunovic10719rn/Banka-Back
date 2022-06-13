package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorSredstvaKapitalDto {

    private String kodValute;
    private double ukupno;
    private double rezervisano;
    private double raspolozivo;
}
