package racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import racun.enums.RacunType;
import racun.model.Racun;
import racun.repository.RacunRepository;

import java.util.UUID;


@Service
public class RacunService {

    private RacunRepository racunRepository;

    @Autowired
    public RacunService(RacunRepository racunRepository){
        this.racunRepository = racunRepository;
    }

    public Racun createRacun(Long userID){
        Racun racun = new Racun();
        racun.setUserID(userID);
        racun.setBrojRacuna(UUID.randomUUID().toString());
        racun.setTipRacuna(RacunType.KES);
        return racunRepository.save(racun);
    }



}
