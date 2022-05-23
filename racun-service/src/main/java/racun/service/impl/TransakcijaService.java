package racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import racun.model.Racun;
import racun.model.Transakcija;
import racun.model.Valuta;
import racun.repository.RacunRepository;
import racun.repository.TransakcijaRepository;

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

    public Transakcija uplata(long userid, String brojRacuna, String opis, Valuta valuta, long uplata){
        Transakcija t = new Transakcija();
        Racun racun = racunRepository.findByBroj(brojRacuna);
        t.setRacun(racun);
        t.setUser_id(userid);
        t.setOpis(opis);
        t.setUplata(uplata);

        sredstvaKapitalService.updateStanje(userid,uplata,0,0,0);
        return transakcijaRepository.save(t);
    }
}
