package racun.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import racun.requests.UplataRequest;
import racun.service.impl.RacunService;
import racun.service.impl.SredstvaKapitalService;
import racun.service.impl.TransakcijaService;

@RestController
@RequestMapping("/api/racun")
public class RacunController {

    private final RacunService racunService;
    private final TransakcijaService transakcijaService;
    private final SredstvaKapitalService sredstvaKapitalService;

    public RacunController(RacunService racunService, TransakcijaService transakcijaService, SredstvaKapitalService sredstvaKapitalService) {
        this.racunService = racunService;
        this.transakcijaService = transakcijaService;
        this.sredstvaKapitalService = sredstvaKapitalService;
    }


    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajRacun(@PathVariable Long id) {
        return ResponseEntity.ok(racunService.createRacun(id));
    }

    @PostMapping(value = "/uplata", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajUplatu(@RequestBody UplataRequest uplataRequest) {
        return ResponseEntity.ok(transakcijaService.uplata(uplataRequest.getUserid(), uplataRequest.getBrojRacuna(), uplataRequest.getOpis(), uplataRequest.getValuta(), uplataRequest.getUplata()));
    }

    /*
    @PatchMapping (value = "/stanje", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> stanjejRacun(@RequestBody SredstvaKapitalaRequest sredstvaKapitalaRequest){
        return ResponseEntity.ok(sredstvaKapitalService.updateStanje(s));
    }
    */


}
