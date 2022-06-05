package rs.edu.raf.banka.racun.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.model.DateFilter;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.requests.RezervacijaRequest;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/racun")
public class RacunController {


    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaService transakcijaService;
    private final UserService userService;

    @Autowired
    public RacunController(SredstvaKapitalService sredstvaKapitalService, TransakcijaService transakcijaService, UserService userService) {
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaService = transakcijaService;
        this.userService = userService;
    }


    @PostMapping(value = "/transakcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajTransakciju(@RequestHeader("Authorization") String token, @RequestBody TransakcijaRequest transakcijaRequest) {
        Transakcija t = transakcijaService.dodajTransakciju(token, transakcijaRequest.getBrojRacuna(), transakcijaRequest.getOpis(), transakcijaRequest.getValutaOznaka(), transakcijaRequest.getOrderId(), transakcijaRequest.getUplata(), transakcijaRequest.getIsplata(), transakcijaRequest.getRezervisano(), transakcijaRequest.getRezervisanoKoristi(), transakcijaRequest.getLastSegment());
        if(t == null) {
            return ResponseEntity.badRequest().body("bad request");
        }
        return ResponseEntity.ok(t);
    }

    @GetMapping(value = "/transakcije", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcije(@RequestHeader("Authorization") String token, @RequestBody(required = false) DateFilter filter) {
        String username = userService.getUserByToken(token);
        if(filter == null || filter.from == null || filter.to == null)
            return ResponseEntity.ok(transakcijaService.getAll(username)); //Pregled svojih transakcija
        return ResponseEntity.ok(transakcijaService.getAll(username, filter.from, filter.to));
    }

    @GetMapping(value = "/transakcije/{valuta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcijeValuta(@RequestHeader("Authorization") String token, @PathVariable String valuta, @RequestBody(required = false) DateFilter filter) {
        String username = userService.getUserByToken(token);
        if(filter == null || filter.from == null || filter.to == null)
            return ResponseEntity.ok(transakcijaService.getAll(username, valuta));
        return ResponseEntity.ok(transakcijaService.getAll(username, valuta, filter.from, filter.to));
    }

    @GetMapping(value = "/stanje/{racun}/{valuta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanje(@RequestHeader("Authorization") String token, @PathVariable String racun, @PathVariable String valuta) {
        String user = userService.getUserByToken(token);
         /*
               TODO Porvera da li je supervizor
            */
        return ResponseEntity.ok(sredstvaKapitalService.getAll(UUID.fromString(racun),valuta));

    }

}
