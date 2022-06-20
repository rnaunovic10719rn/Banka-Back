package rs.edu.raf.banka.racun.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyContactPersonRequest {

    private Long id;
    private Long companyId;
    private String ime;
    private String prezime;
    private String email;
    private String brojTelefona;
    private String pozicija;
    private String napomena;

}
