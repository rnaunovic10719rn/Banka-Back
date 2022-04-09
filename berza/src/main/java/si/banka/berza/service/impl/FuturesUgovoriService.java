package si.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import si.banka.berza.model.Akcije;
import si.banka.berza.repository.AkcijeRepository;

import java.util.List;

@Service
public class FutusresUgovoriService {

    private FuturesUgovoriRepository futuresUgovoriRepository;


    @Autowired
    public FutusresUgovoriService(FuturesUgovoriRepository futuresUgovoriRepository){
        this.futuresUgovoriRepository = futuresUgovoriRepository;
    }

    public List<Akcije> getAllAkcije(){

        return futuresUgovoriRepository.findAll();
    }

    public Page<Akcije> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
        Akcije akcije = new Akcije();
        akcije.setOznaka_hartije(oznakaHartije);
        akcije.setOpis_hartije(opisHartije);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznaka_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opis_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Akcije> example = Example.of(akcije, exampleMatcher);

        return futuresUgovoriRepository.findAll(example, PageRequest.of(page, size));
    }

    public Page<Akcije> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
        List<Akcije> akcije = futuresUgovoriRepository.filterAkcije(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
        return new PageImpl<Akcije>(akcije, PageRequest.of(page, size), akcije.size());
    }

    public Akcije getByID(Long id){
        return futuresUgovoriRepository.findAkcijeById_hartije_od_vrednosti(id);
    }
}
