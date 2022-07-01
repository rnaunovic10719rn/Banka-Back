package rs.edu.raf.banka.berza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Valuta implements Serializable {

    @Serial
    private static final long serialVersionUID = -895627472796468918L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kodValute;
    private String nazivValute;
    private String oznakaValute;

    @OneToMany
    @JsonIgnore
    private Set<IstorijaInflacije> istorijaInflacije;

}
