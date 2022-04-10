package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;

import java.util.List;

@Service
public class AkcijeService {

    private AkcijeRepository akcijeRepository;


    @Autowired
    public AkcijeService(AkcijeRepository akcijeRepository){
        this.akcijeRepository = akcijeRepository;
    }

    public List<Akcije> getAllAkcije(){
        return akcijeRepository.findAll();
    }

    public Page<Akcije> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
        Akcije akcije = new Akcije();
        akcije.setOznaka_hartije(oznakaHartije);
        akcije.setOpis_hartije(opisHartije);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznaka_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opis_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Akcije> example = Example.of(akcije, exampleMatcher);

        return akcijeRepository.findAll(example, PageRequest.of(page, size));
    }

    public Page<Akcije> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
        List<Akcije> akcije = akcijeRepository.filterAkcije(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
        return new PageImpl<Akcije>(akcije, PageRequest.of(page, size), akcije.size());
    }

    public Akcije getByID(Long id){
        return akcijeRepository.findAkcijeById(id);
    }
}
