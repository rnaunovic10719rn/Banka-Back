package rs.edu.raf.banka.berza.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;

@RestController
@RequestMapping("/api/futures/podaci")
public class FuturesUgovoriPodaciController {

    private final FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    public FuturesUgovoriPodaciController(FuturesUgovoriPodaciService futuresUgovoriPodaciService){
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOdabraniFuturesUgovori(){
        return ResponseEntity.ok(futuresUgovoriPodaciService.getOdabraniFuturesUgovori());
    }

    @GetMapping(value = "/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFuturesUgovor(@PathVariable String symbol){
        if(symbol == null || symbol.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(futuresUgovoriPodaciService.getFuturesUgovor(symbol));
    }

//    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> getFuturesUgovor(@PathVariable Long id){
//        return ResponseEntity.ok(futuresUgovoriPodaciService.getFuturesUgovor(id));
//    }

}
