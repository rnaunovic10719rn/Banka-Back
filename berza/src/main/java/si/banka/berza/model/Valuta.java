package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Valuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_valute;

    private String kod_valute;
    private String naziv_valute;
    private String oznaka_valute;
    private String drzava;
    private Map<Date, Double> istorija_inflacije;

}
