package rs.edu.raf.banka.berza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Berza implements Serializable {

    @Serial
    private static final long serialVersionUID = 6879714582042601486L;

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

    @ManyToOne(fetch = FetchType.EAGER)
    private Valuta valuta;

    @OneToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Order> orderi;

}
