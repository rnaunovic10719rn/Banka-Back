package rs.edu.raf.banka.berza.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarginTransakcijaResponse {
    private Long id;
    private String tip;

    private Date datumVreme;

    private Long orderId;
    private String username;
    private String opis;

    private double ulog;
    private double loanValue;
    private double maintenanceMargin;
    private double kamata;
    private double unitPrice;

    private String kapitalType;
    private Long hartijeOdVrednostiID;
}
