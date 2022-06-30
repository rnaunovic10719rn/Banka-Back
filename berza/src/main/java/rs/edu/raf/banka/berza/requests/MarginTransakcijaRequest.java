package rs.edu.raf.banka.berza.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarginTransakcijaRequest {

    private MarginTransakcijaType tipTranskacije;
    private String opis;
    private Long orderId;
    private double iznos;
    private double kredit;
    private double maintenanceMargin;
    private TransakcijaKapitalType tipKapitala;
    private long hartijaId;
    private String valutaOznaka;
    private Double kolicina;
    private Double unitPrice;
    private String username;

}
