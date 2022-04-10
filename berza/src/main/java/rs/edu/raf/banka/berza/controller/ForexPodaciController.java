package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.ForexDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.dto.request.ForexTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.requests.FilterHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.requests.SearchHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forex/podaci")
public class ForexPodaciController {

    private final ForexPodaciService forexPodaciService;

    @Autowired
    private ModelMapper modelMapper;


    public ForexPodaciController(ForexPodaciService forexPodaciService){
        this.forexPodaciService = forexPodaciService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForex(){
        return ResponseEntity.ok(forexPodaciService.getOdabraniParovi());
    }

    @GetMapping(value = "/{from}/{to}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForexById(@PathVariable String from, @PathVariable String to){
        if(from == null || from.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if(to == null || to.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forexPodaciService.getForexBySymbol(from, to));
    }

    @GetMapping(value = "/timeseries/{type}/{symbolFrom}/{symbolTo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeTimeseries(@PathVariable String type, @PathVariable String symbolFrom, @PathVariable String symbolTo){
        if(type == null || type.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if(symbolTo == null || symbolTo.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if(symbolFrom == null || symbolFrom.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ForexTimeseriesUpdateRequest req = ForexTimeseriesUpdateRequest.getForType(type, symbolTo, symbolFrom);
        if(req == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(forexPodaciService.getForexTimeseries(req));
    }

//    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> getForex(){
//        List<Forex> forexList = forexPodaciService.getAllForex();
//        return ResponseEntity.ok(forexList.stream().map(this::convertToDto).collect(Collectors.toList()));
//    }
//
//    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> getForexById(@PathVariable Long id){
//        return ResponseEntity.ok(forexPodaciService.getByID(id));
//    }
//
//    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> searchForex(@RequestBody SearchHartijaOdVrednostiRequest searchHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
//        return ResponseEntity.ok(forexPodaciService.search(searchHartijaOdVrednostiRequest.getOznaka_hartije(), searchHartijaOdVrednostiRequest.getOpis_hartije(),
//                page, size));
//    }
//
//    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> filterForex(@RequestBody FilterHartijaOdVrednostiRequest filterHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
//        String berzaPrefix = filterHartijaOdVrednostiRequest.getBerzaPrefix();
//        Double priceLowBound = filterHartijaOdVrednostiRequest.getPriceLowBound();
//        Double priceUpperBound = filterHartijaOdVrednostiRequest.getPriceUpperBound();
//        Double askLowBound = filterHartijaOdVrednostiRequest.getAskLowBound();
//        Double askUpperBound = filterHartijaOdVrednostiRequest.getAskUpperBound();
//        Double bidLowBound = filterHartijaOdVrednostiRequest.getBidLowBound();
//        Double bidUpperBound = filterHartijaOdVrednostiRequest.getBidUpperBound();
//        Long volumeLowBound = filterHartijaOdVrednostiRequest.getVolumeLowBound();
//        Long volumeUpperBound = filterHartijaOdVrednostiRequest.getVolumeUpperBound();
//
//        return ResponseEntity.ok(forexPodaciService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
//                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page,size));
//    }
//
//    private ForexDto convertToDto(Forex forex) {
//        ForexDto forexDto = modelMapper.map(forex, ForexDto.class);
//        return forexDto;
//    }

}
