package si.banka.berza.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Berza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_berze;

    private String oznaka_berze;
    private String naziv_name;
    private String drzava;
    private Valuta valuta;
    private String vremenska_zona;
    private String pre_market_radno_vreme;
    private String post_market_radno_vreme;
    private List<Date> praznici;



}
