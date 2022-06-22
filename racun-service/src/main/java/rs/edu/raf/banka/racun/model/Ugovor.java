package rs.edu.raf.banka.racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.UgovorStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ugovor
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String company;

    UgovorStatus status;
    Date created;
    Date lastChanged;

    String delodavniBroj;

    String description;

    Long documentId = -1L;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ugovor", fetch = FetchType.LAZY, orphanRemoval = true)
    List<TransakcionaStavka> stavke;

    @PrePersist
    protected void onCreate() {
        created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastChanged = new Date();
    }
}
