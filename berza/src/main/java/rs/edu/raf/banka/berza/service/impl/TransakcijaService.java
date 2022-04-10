package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.model.Transakcija;
import rs.edu.raf.banka.berza.repository.TranskacijaRepository;

import java.util.Date;
import java.util.List;

@Service
public class TransakcijaService {

    private TranskacijaRepository transkacijaRepository;

    @Autowired
    public TransakcijaService(TranskacijaRepository transkacijaRepository){
        this.transkacijaRepository = transkacijaRepository;
    }


    public Transakcija saveTranskacija(Transakcija transakcija){
        return transkacijaRepository.save(transakcija);
    }

    public List<Double> findPriceActionBuy(Double bid){
        return transkacijaRepository.findCeneTransakcijaBuy(new Date(), bid);
    }

    public List<Double> findPriceActionSell(Double ask){
        return transkacijaRepository.findCeneTransakcijaSell(new Date(), ask);
    }
}
