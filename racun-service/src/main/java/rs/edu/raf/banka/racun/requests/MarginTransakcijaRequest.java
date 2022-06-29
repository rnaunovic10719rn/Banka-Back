package rs.edu.raf.banka.racun.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.MarginTransakcijaType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarginTransakcijaRequest {

    @JsonIgnore
    private UUID brojRacuna;

    private MarginTransakcijaType tipTranskacije;
    private String opis;
    private Long orderId;
    private double iznos;
    private double kredit;
    private double maintenanceMargin;
    private KapitalType tipKapitala;
    private long hartijaId;
    private String valutaOznaka;
    private Double kolicina;
    private Double unitPrice;
    private String username;

}
