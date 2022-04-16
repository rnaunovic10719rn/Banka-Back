package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.request.ForexTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.service.impl.ForexPodaciService;

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



}
