package si.banka.berza.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Berza {
    private String oznaka_berze;
    private String naziv_name;
    private String drzava;
    private Valuta valuta;
    private String vremenska_zona;
    private String pre_market_radno_vreme;
    private String post_market_radno_vreme;
    private List<Date> praznici;



}
