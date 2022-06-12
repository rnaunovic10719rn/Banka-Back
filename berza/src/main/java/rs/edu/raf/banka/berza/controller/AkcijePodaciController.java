package rs.edu.raf.banka.berza.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.service.impl.AkcijePodaciService;

@RestController
@RequestMapping("/api/akcije/podaci")
public class AkcijePodaciController {

    private final AkcijePodaciService akcijePodaciService;

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

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeById(@PathVariable Long id){
        if(id == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(akcijePodaciService.getAkcijaById(id));
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

}
