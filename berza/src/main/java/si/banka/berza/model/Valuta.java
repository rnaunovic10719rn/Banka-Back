package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Valuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_valute;

    private String kod_valute;
    private String naziv_valute;
    private String oznaka_valute;
    private String drzava;

    @OneToMany
    private List<IstorijaInflacije> istorija_inflacije;

//    private Map<Date, Double> istorija_inflacije;

}
