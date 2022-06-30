package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Akcije extends HartijaOdVrednosti{

    private Long outstandingShares;

}
