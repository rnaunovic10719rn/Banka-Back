package rs.edu.raf.banka.berza.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Berza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oznakaBerze;
    private String naziv;
    private String drzava;
    private String micCode;
    private String openTime;
    private String closeTime;
    private String vremenskaZona;

    @ManyToOne
    private Valuta valuta;

    @OneToMany
    private List<Order> orderi;

}
