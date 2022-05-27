package racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import racun.model.Racun;
import racun.model.Transakcija;
import racun.model.Valuta;
import racun.repository.RacunRepository;
import racun.repository.TransakcijaRepository;

import java.util.List;

@Service
public class TransakcijaService {

    private final RacunRepository racunRepository;
    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaRepository transakcijaRepository;

    @Autowired
    public TransakcijaService(RacunRepository racunRepository, SredstvaKapitalService sredstvaKapitalService, TransakcijaRepository transakcijaRepository) {
        this.racunRepository = racunRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaRepository = transakcijaRepository;
    }

    public List<Transakcija> getAll(String username){
        return transakcijaRepository.findByUsername(username);
    }

    public Transakcija uplata(String username, String brojRacuna, String opis, int valuta, long uplata){
        Transakcija t = new Transakcija();
        Racun racun = racunRepository.findByBroj(brojRacuna);
        t.setRacun(racun);
        t.setUsername(username);
        t.setOpis(opis);
        t.setUplata(uplata);

        sredstvaKapitalService.updateStanje(username,brojRacuna,uplata,0,0,0);
        return transakcijaRepository.save(t);
    }

    public Transakcija isplata(String username, String brojRacuna, String opis, int valuta, long isplata){
        Transakcija t = new Transakcija();
        Racun racun = racunRepository.findByBroj(brojRacuna);
        t.setRacun(racun);
        t.setUsername(username);
        t.setOpis(opis);
        t.setIsplata(isplata);

        sredstvaKapitalService.updateStanje(username,brojRacuna,0,isplata,0,0);
        return transakcijaRepository.save(t);
    }


}
