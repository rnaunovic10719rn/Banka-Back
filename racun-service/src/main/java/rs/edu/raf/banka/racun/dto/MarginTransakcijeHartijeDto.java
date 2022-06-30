package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarginTransakcijeHartijeDto {
    Date datum;
    String tipOrdera;
    Double cena;
    Long kolicina;
    Double ukupno;
}
