package rs.edu.raf.banka.racun.model.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.company.Company;

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

    @ManyToOne
    @JoinColumn(name = "company_id")
    Company company;

    UgovorStatus status = UgovorStatus.DRAFT;
    Date created;
    Date lastChanged;

    String delovodniBroj;

    String description;

    String documentId = "";

    Long agentId = -1L;


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
