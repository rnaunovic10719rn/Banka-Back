package si.banka.berza.model;

import java.util.Date;
import java.util.List;

public abstract class HartijaOdVrednosti {
    private String oznaka_hartije;
    private String opis_hartije;
    private Berza berza;
    private Date last_updated;
    private double cena;
    private double ask;
    private double bid;
    private double promena_iznos;
    private long volume;
    private List<String> istorijski_podaci;


}
