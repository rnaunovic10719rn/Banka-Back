package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;

import java.util.List;

@Service
public class FuturesUgovoriService {

    private FuturesUgovoriRepository futuresUgovoriRepository;


    @Autowired
    public FuturesUgovoriService(FuturesUgovoriRepository futuresUgovoriRepository){
        this.futuresUgovoriRepository = futuresUgovoriRepository;
    }

    public List<FuturesUgovori> getAllFuturesUgovori(){

        return futuresUgovoriRepository.findAll();
    }

    public Page<FuturesUgovori> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
        FuturesUgovori futuresUgovori = new FuturesUgovori();
        futuresUgovori.setOznaka_hartije(oznakaHartije);
        futuresUgovori.setOpis_hartije(opisHartije);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznaka_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opis_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<FuturesUgovori> example = Example.of(futuresUgovori, exampleMatcher);

        return futuresUgovoriRepository.findAll(example, PageRequest.of(page, size));
    }

    public Page<FuturesUgovori> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
        List<FuturesUgovori> ugovori = futuresUgovoriRepository.filterFuturesUgovori(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
        return new PageImpl<FuturesUgovori>(ugovori, PageRequest.of(page, size), ugovori.size());
    }

    public FuturesUgovori getByID(Long id){
        return futuresUgovoriRepository.findFuturesById(id);
    }
}
