package si.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import si.banka.berza.model.Akcije;
import si.banka.berza.model.Forex;
import si.banka.berza.repository.ForexRepository;

import java.util.List;

@Service
public class ForexService {

    private ForexRepository forexRepository;


    @Autowired
    public ForexService(ForexRepository forexRepository){
        this.forexRepository = forexRepository;
    }

    public List<Forex> getAllForex(){
        return forexRepository.findAll();
    }

    public Page<Forex> search(String oznakaHartije, String opisHartije, Integer page, Integer size){
        Forex forex = new Forex();
        forex.setOznaka_hartije(oznakaHartije);
        forex.setOpis_hartije(opisHartije);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("oznaka_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("opis_hartije", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Forex> example = Example.of(forex, exampleMatcher);

        return forexRepository.findAll(example, PageRequest.of(page, size));
    }

    public Page<Forex> filter(String berzaPrefix, Double priceLowBound, Double priceUpperBound, Double askLowBound, Double askUpperBound,
                               Double bidLowBound, Double bidUpperBound, Long volumeLowBound, Long volumeUpperBound, Integer page, Integer size){
        List<Forex> forexList = forexRepository.filterForex(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound, bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound);
        return new PageImpl<Forex>(forexList, PageRequest.of(page, size), forexList.size());
    }

    public Forex getByID(Long id){
        return forexRepository.findForexById(id);
    }

}
