package rs.edu.raf.banka.berza.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransakcijaResponse {
    private Long id;

    private Date datumVreme;

    private Long orderId;
    private String username;
    private String opis;

    private double uplata;
    private double isplata;
    private double rezervisano;
    private double rezervisanoKoristi;

}
