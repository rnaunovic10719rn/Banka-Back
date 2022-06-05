package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TransakcijaService {

    private final RacunRepository racunRepository;
    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaRepository transakcijaRepository;
    private final ValutaRepository valutaRepository;

    @Autowired
    public TransakcijaService(RacunRepository racunRepository, SredstvaKapitalService sredstvaKapitalService, TransakcijaRepository transakcijaRepository, ValutaRepository valutaRepository) {
        this.racunRepository = racunRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaRepository = transakcijaRepository;
        this.valutaRepository = valutaRepository;
    }

    public List<Transakcija> getAll(String username){
        return transakcijaRepository.findByUsername(username);
    }

    public List<Transakcija> getAll(String username, Date odFilter, Date doFilter){
        return transakcijaRepository.findByUsername(username);
    }

    public List<Transakcija> getAll(String username, String valuta){
        Valuta val = valutaRepository.findValutaByOznakaValute(valuta);
        return transakcijaRepository.findByUsername(username, val);
    }

    public List<Transakcija> getAll(String username, String valuta, Date odFilter, Date doFilter){
        Valuta val = valutaRepository.findValutaByOznakaValute(valuta);
        return transakcijaRepository.findByUsername(username, val, odFilter, doFilter);
    }

    public Transakcija dodajTransakciju(String username, UUID brojRacuna, String opis, String valuta, double iznos){
        if (sredstvaKapitalService.updateStanje(brojRacuna,iznos,0,0,valuta,0)!=null){
            Transakcija t = new Transakcija();
            Racun racun = racunRepository.findByBrojRacuna(brojRacuna);
            t.setRacun(racun);
            t.setUsername(username);
            t.setValuta(valutaRepository.findValutaByOznakaValute(valuta));
            t.setOpis(opis);
            if (iznos>0){
                t.setUplata(iznos);
            }else {
                t.setIsplata(Math.abs(iznos));
            }
            return transakcijaRepository.save(t);
        }
        return null;
    }

    public Transakcija rezervacija(String username, UUID brojRacuna, String opis, String valuta, double iznos,double rezervisanoKoristi,long hartijeOdVrendostiID){
        if (sredstvaKapitalService.updateStanje(brojRacuna,iznos,0,rezervisanoKoristi,valuta,hartijeOdVrendostiID)!=null) {
            Transakcija t = new Transakcija();
            Racun racun = racunRepository.findByBrojRacuna(brojRacuna);
            t.setRacun(racun);
            t.setUsername(username);
            t.setValuta(valutaRepository.findValutaByOznakaValute(valuta));
            t.setRezervisanoKoristi(rezervisanoKoristi);
            t.setOpis(opis);
            t.setIsplata(iznos);

            return transakcijaRepository.save(t);
        }
        return null;
    }



}
