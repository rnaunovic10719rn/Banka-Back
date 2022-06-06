package rs.edu.raf.banka.racun.requests;

import lombok.Data;
import rs.edu.raf.banka.racun.enums.KapitalType;

import java.util.UUID;

@Data
public class TransakcijaRequest {

    private UUID brojRacuna;
    private String opis;
    private String valutaOznaka;
    private Long orderId;
    private double uplata;
    private double isplata;
    private double rezervisano;
    private double rezervisanoKoristi;
    private Boolean lastSegment;
    private KapitalType type;
    private long hartijaId;

}
