package rs.edu.raf.banka.racun.requests;

import lombok.Data;

import java.util.UUID;

@Data
public class TransakcijaRequest {

    private UUID brojRacuna;
    private String opis;
    private String valutaOznaka;
    private double iznos;


}
