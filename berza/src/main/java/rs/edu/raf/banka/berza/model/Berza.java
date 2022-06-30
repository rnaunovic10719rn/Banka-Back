package rs.edu.raf.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

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
