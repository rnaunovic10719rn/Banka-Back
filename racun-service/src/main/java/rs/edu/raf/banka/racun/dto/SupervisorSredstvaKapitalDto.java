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
    private double kredit = 0.0;
    private double maintenanceMargin = 0.0;
    private boolean marginCall = false;

}
