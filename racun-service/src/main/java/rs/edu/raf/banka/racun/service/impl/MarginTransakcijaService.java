package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.repository.MarginTransakcijaRepository;

@Service
public class MarginTransakcijaService {

    private MarginTransakcijaRepository marginTransakcijaRepository;

    @Autowired
    public MarginTransakcijaService(MarginTransakcijaRepository marginTransakcijaRepository) {
        this.marginTransakcijaRepository = marginTransakcijaRepository;
    }
}
