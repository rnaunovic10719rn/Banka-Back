package rs.edu.raf.banka.racun.model.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.KapitalType;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransakcionaStavka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    Ugovor ugovor;

    Long userId = -1L;

    /**
     * POTRAZNA STRANA -- STA MI DAJEMO KUPCU
     */

    KapitalType kapitalTypePotrazni;
    Long kapitalPotrazniId;
    String kapitalPotrazniOznaka;
    Double kolicinaPotrazna;

    /**
     * DUGOVNA STRANA -- STA KUPAC DAJE NAMA
     */

    KapitalType kapitalTypeDugovni;
    Long kapitalDugovniId;
    String kapitalDugovniOznaka;
    Double kolicinaDugovna;

    public TransakcionaStavka(TransakcionaStavka stavka) {
        this.ugovor = stavka.ugovor;
        this.userId = stavka.userId;
        this.kapitalTypePotrazni = stavka.kapitalTypePotrazni;
        this.kapitalPotrazniId = stavka.kapitalPotrazniId;
        this.kapitalPotrazniOznaka = stavka.kapitalPotrazniOznaka;
        this.kolicinaPotrazna = stavka.kolicinaPotrazna;
        this.kapitalTypeDugovni = stavka.kapitalTypeDugovni;
        this.kapitalDugovniId = stavka.kapitalDugovniId;
        this.kapitalDugovniOznaka = stavka.kapitalDugovniOznaka;
        this.kolicinaDugovna = stavka.kolicinaDugovna;
    }
}
