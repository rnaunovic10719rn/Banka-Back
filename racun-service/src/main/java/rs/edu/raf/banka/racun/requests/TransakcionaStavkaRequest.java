package rs.edu.raf.banka.racun.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import rs.edu.raf.banka.racun.enums.KapitalType;

@Data
public class TransakcionaStavkaRequest {
    // Prosledjuje se jedan ili drugi
    Long ugovorId;
    Long stavkaId;

    /**
     * POTRAZNA STRANA -- STA MI DAJEMO KUPCU
     */

    KapitalType kapitalTypePotrazni;
    String kapitalOznakaPotrazni;
    Double kolicinaPotrazna;

    @JsonIgnore
    Long kapitalPotrazniId;

    /**
     * DUGOVNA STRANA -- STA KUPAC DAJE NAMA
     */

    KapitalType kapitalTypeDugovni;
    String kapitalOznakaDugovni;
    Double kolicinaDugovna;

    @JsonIgnore
    Long kapitalDugovniId;
}
