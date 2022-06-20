package rs.edu.raf.banka.racun.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequest {

    private Long id;
    private String naziv;
    private String maticniBroj;
    private String pib;
    private String sifraDelatnosti;
    private String adresa;
    private String drzava;

}
