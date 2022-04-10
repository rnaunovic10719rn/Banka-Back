package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Valuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String KodValute;
    private String NazivValute;
    private String OznakaValute;
    private String Drzava;

    @OneToMany
    private List<IstorijaInflacije> IstorijaInflacije;
}
