package rs.edu.raf.banka.racun.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.UgovorStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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

    Long documentId;


    @OneToMany
    @JoinColumn(name = "ugovor_id")
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
