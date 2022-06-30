package rs.edu.raf.banka.berza.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@MappedSuperclass
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class HartijaOdVrednosti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String oznakaHartije;

    private String opisHartije;

    @ManyToOne
    private Berza berza;

    private Date lastUpdated;

    private Boolean custom = false;
}
