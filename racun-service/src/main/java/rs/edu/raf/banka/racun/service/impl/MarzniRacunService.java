package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.repository.MarzniRacunRepository;

@Service
public class MarzniRacunService {

    private MarzniRacunRepository marzniRacunRepository;

    @Autowired
    public MarzniRacunService(MarzniRacunRepository marzniRacunRepository){
        this.marzniRacunRepository = marzniRacunRepository;
    }
}
