package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.ForexDto;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.requests.FilterHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.requests.SearchHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.service.impl.ForexService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forex")
public class ForexController {

    private final ForexService forexService;

    @Autowired
    private ModelMapper modelMapper;


    public ForexController(ForexService forexService){
        this.forexService = forexService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForex(){
        List<Forex> forexList = forexService.getAllForex();
        return ResponseEntity.ok(forexList.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForexById(@PathVariable Long id){
        return ResponseEntity.ok(forexService.getByID(id));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchForex(@RequestBody SearchHartijaOdVrednostiRequest searchHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
        return ResponseEntity.ok(forexService.search(searchHartijaOdVrednostiRequest.getOznaka_hartije(), searchHartijaOdVrednostiRequest.getOpis_hartije(),
                page, size));
    }

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filterForex(@RequestBody FilterHartijaOdVrednostiRequest filterHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
        String berzaPrefix = filterHartijaOdVrednostiRequest.getBerzaPrefix();
        Double priceLowBound = filterHartijaOdVrednostiRequest.getPriceLowBound();
        Double priceUpperBound = filterHartijaOdVrednostiRequest.getPriceUpperBound();
        Double askLowBound = filterHartijaOdVrednostiRequest.getAskLowBound();
        Double askUpperBound = filterHartijaOdVrednostiRequest.getAskUpperBound();
        Double bidLowBound = filterHartijaOdVrednostiRequest.getBidLowBound();
        Double bidUpperBound = filterHartijaOdVrednostiRequest.getBidUpperBound();
        Long volumeLowBound = filterHartijaOdVrednostiRequest.getVolumeLowBound();
        Long volumeUpperBound = filterHartijaOdVrednostiRequest.getVolumeUpperBound();

        return ResponseEntity.ok(forexService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page,size));
    }

    private ForexDto convertToDto(Forex forex) {
        ForexDto forexDto = modelMapper.map(forex, ForexDto.class);
        return forexDto;
    }

}
