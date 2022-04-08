package si.banka.berza.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuturesUgovori extends HartijaOdVrednosti{

    private Double contract_size;
    private String contract_unit;
    private Double maintenance_margin;
    private Date settlement_date;

    //TODO: Dodati izvedene informacije koje su opisane u specifikaciji.
}
