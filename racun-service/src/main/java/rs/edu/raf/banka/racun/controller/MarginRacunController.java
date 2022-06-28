package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.requests.MarginTransakcijaRequest;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.MarginTransakcijaService;

@RestController
@RequestMapping("/api/margin")
public class MarginRacunController {

    private final MarginTransakcijaService marginTransakcijaService;

    @Autowired
    public MarginRacunController(MarginTransakcijaService marginTransakcijaService) {
        this.marginTransakcijaService = marginTransakcijaService;
    }

    @PostMapping(value = "/transakcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajTransakciju(@RequestHeader("Authorization") String token, @RequestBody MarginTransakcijaRequest request) {
        MarginTransakcija mt = marginTransakcijaService.dodajTransakciju(token, request);
        if (mt == null) {
            return ResponseEntity.badRequest().body("bad request");
        }
        return ResponseEntity.ok(mt);
    }

}
