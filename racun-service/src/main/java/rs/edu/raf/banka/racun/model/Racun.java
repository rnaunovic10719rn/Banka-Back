package rs.edu.raf.banka.racun.model;

import lombok.*;
import rs.edu.raf.banka.racun.enums.RacunType;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Racun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID brojRacuna;
    private RacunType tipRacuna;

}
