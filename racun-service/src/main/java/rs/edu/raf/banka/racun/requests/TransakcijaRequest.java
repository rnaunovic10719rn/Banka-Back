package rs.edu.raf.banka.racun.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import rs.edu.raf.banka.racun.enums.KapitalType;

import java.util.UUID;

@Data
public class TransakcijaRequest {

    @JsonIgnore
    private UUID brojRacuna;

    private boolean margins;
    private String opis;
    private String valutaOznaka;
    private Long orderId;
    private double uplata;
    private double isplata;
    private double rezervisano;
    private Boolean lastSegment = false;
    private KapitalType type;
    private long hartijaId;

}
