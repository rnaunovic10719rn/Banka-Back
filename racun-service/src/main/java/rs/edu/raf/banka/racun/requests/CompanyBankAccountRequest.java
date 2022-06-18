package rs.edu.raf.banka.racun.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBankAccountRequest {

    private Long id;
    private Long companyId;
    private Long valutaId;
    private String brojRacuna;
    private String banka;
    private Boolean active;

}
