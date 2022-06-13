package rs.edu.raf.banka.berza.requests;

import lombok.Data;

@Data
public class TransakcijaRequest {

    private String opis;
    private String valutaOznaka;
    private Long orderId;
    private double uplata;
    private double isplata;
    private double rezervisano;
    private double unitPrice;
    private Boolean lastSegment;
    private TransakcijaKapitalType type;
    private long hartijaId;
    private String username;

}
