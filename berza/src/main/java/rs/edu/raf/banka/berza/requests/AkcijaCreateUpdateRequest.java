package rs.edu.raf.banka.berza.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AkcijaCreateUpdateRequest {

    private Long id;

    private String oznaka;
    private String opis;
    private String berzaOznaka;

    private Long outstandingShares;

}
