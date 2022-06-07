package rs.edu.raf.banka.racun.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KapitalStanje {
    double ukupno;
    double novac;
    double forex;
    double future;
    double akcija;
}
