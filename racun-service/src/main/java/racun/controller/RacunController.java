package racun.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import racun.service.impl.RacunService;

@RestController
@RequestMapping("/api/racun")
public class RacunController {

    private final RacunService racunService;

    public RacunController(RacunService racunService){
        this.racunService = racunService;
    }


    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajRacun(@PathVariable Long id){
        return ResponseEntity.ok(racunService.createRacun(id));
    }

    @PatchMapping (value = "/stanje", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> stanjejRacun(@PathVariable Long id){
        return ResponseEntity.ok(racunService.createRacun(id));
    }




}
