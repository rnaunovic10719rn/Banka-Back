package rs.edu.raf.banka.berza.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Berza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oznakaBerze;
    private String naziv;
    private String drzava;
    private String micCode;
    private String valutaString;
    private String openTime;
    private String closeTime;

    @ManyToOne
    private Valuta valuta;

    private String vremenskaZona;
    private String pre_market_radno_vreme;
    private String post_market_radno_vreme;

    @ElementCollection
    private List<Date> praznici;

    @OneToMany
    private List<Order> orderi;

}
