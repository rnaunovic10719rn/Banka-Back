package rs.edu.raf.banka.berza.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class IstorijaInflacije {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Date datum;

    @Column
    private Double procenatInflacije;

}
