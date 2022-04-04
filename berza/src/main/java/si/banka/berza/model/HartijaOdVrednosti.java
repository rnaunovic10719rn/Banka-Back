package si.banka.berza.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class HartijaOdVrednosti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_hartija_od_vrednosti;

    private String oznaka_hartije;
    private String opis_hartije;
    private Berza berza;
    private Date last_updated;
    private double cena;
    private double ask;
    private double bid;
    private double promena_iznos;
    private long volume;
    private List<String> istorijski_podaci;
}
