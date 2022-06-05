package rs.edu.raf.banka.berza.service.impl;

import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.model.HartijaOdVrednosti;
import rs.edu.raf.banka.berza.repository.HartijaRepository;

import java.util.List;

@Service
public class HartijaService
{
    private HartijaRepository hartijaRepository;

    public HartijaService(HartijaRepository hartijaRepository){
        this.hartijaRepository = hartijaRepository;
    }

    //TODO: Call influx update on selected types
    public List<HartijaOdVrednosti> getAllNearSettlement() {
        return hartijaRepository.getAllNearSettlement();
    }
}
