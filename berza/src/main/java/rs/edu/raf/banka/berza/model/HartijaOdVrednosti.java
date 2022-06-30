package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

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
