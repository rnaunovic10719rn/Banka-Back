package rs.edu.raf.banka.racun.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.service.impl.MarzniRacunService;

@RestController
@RequestMapping("/api/marzniRacun")
public class MarzniRacunController {

    private final MarzniRacunService marzniRacunService;

    public MarzniRacunController(MarzniRacunService marzniRacunService) {
        this.marzniRacunService = marzniRacunService;
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPodaciOStanjuRacuna(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        return null;
    }
    
}
