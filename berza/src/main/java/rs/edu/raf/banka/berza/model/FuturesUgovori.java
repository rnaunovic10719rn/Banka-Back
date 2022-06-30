package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuturesUgovori extends HartijaOdVrednosti{

    private Double contractSize;
    private String contractUnit;
    private Double maintenanceMargin;
    private Date settlementDate;

    //TODO: Dodati izvedene informacije koje su opisane u specifikaciji.
}
