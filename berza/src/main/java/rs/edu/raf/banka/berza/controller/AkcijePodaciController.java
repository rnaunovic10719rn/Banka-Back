package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.service.impl.AkcijePodaciService;


@RestController
@RequestMapping("/api/akcije/podaci")
public class AkcijePodaciController {

    private final AkcijePodaciService akcijePodaciService;

    @Autowired
    private ModelMapper modelMapper;

    public AkcijePodaciController(AkcijePodaciService akcijePodaciService){
        this.akcijePodaciService = akcijePodaciService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOdabraneAkcije(){
        return ResponseEntity.ok(akcijePodaciService.getOdabraneAkcije());
    }

    @GetMapping(value = "/{ticker}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeById(@PathVariable String ticker){
        if(ticker == null || ticker.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(akcijePodaciService.getAkcijaByTicker(ticker));
    }

    @GetMapping(value = "/timeseries/{type}/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeTimeseries(@PathVariable String type, @PathVariable String symbol){
        if(type == null || type.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if(symbol == null || symbol.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        AkcijeTimeseriesUpdateRequest req = AkcijeTimeseriesUpdateRequest.getForType(type, symbol);
        if(req == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(akcijePodaciService.getAkcijeTimeseries(req));
    }

    //    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> searchAkcije(@RequestBody SearchHartijaOdVrednostiRequest searchHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
//        return ResponseEntity.ok(akcijeService.search(searchHartijaOdVrednostiRequest.getOznaka_hartije(), searchHartijaOdVrednostiRequest.getOpis_hartije(),
//                page, size));
//    }
//
//    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> filterAkcije(@RequestBody FilterHartijaOdVrednostiRequest filterHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
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
//        return ResponseEntity.ok(akcijeService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
//                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page,size));
//    }
//
//    private AkcijeDto convertToDto(Akcije akcije) {
//        AkcijeDto akcijeDto = modelMapper.map(akcije, AkcijeDto.class);
//        return akcijeDto;
//    }

}
