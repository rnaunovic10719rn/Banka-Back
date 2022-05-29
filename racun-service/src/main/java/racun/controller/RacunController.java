package racun.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import racun.requests.RezervacijaRequest;
import racun.requests.TransakcijaRequest;
import racun.service.impl.SredstvaKapitalService;
import racun.service.impl.TransakcijaService;
import racun.service.impl.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/racun")
public class RacunController {


    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaService transakcijaService;
    private final UserService userService;

    public RacunController(SredstvaKapitalService sredstvaKapitalService, TransakcijaService transakcijaService, UserService userService) {
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaService = transakcijaService;
        this.userService = userService;
    }


    @PostMapping(value = "/transakcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajTransakciju(@RequestHeader("Authorization") String token, @RequestBody TransakcijaRequest transakcijaRequest) {
        String username = userService.getUserByToken(token); //Read id from token
        return ResponseEntity.ok(transakcijaService.dodajTransakciju(username, transakcijaRequest.getBrojRacuna(), transakcijaRequest.getOpis(), transakcijaRequest.getValutaOznaka(), transakcijaRequest.getIznos()));
    }

    @GetMapping(value = "/transakcije", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcije(@RequestHeader("Authorization") String token) {
        String username = userService.getUserByToken(token);
        return ResponseEntity.ok(transakcijaService.getAll(username)); //Pregled svojih transakcija
    }

    @GetMapping(value = "/stanje/{racun}/{valuta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanje(@RequestHeader("Authorization") String token, @PathVariable String racun, @PathVariable String valuta) {
        String user = userService.getUserByToken(token);
         /*
               TODO Porvera da li je supervizor
            */
        return ResponseEntity.ok(sredstvaKapitalService.getAll(UUID.fromString(racun),valuta));

    }

    @PostMapping(value = "/rezervacija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajRezervaciju(@RequestHeader("Authorization") String token, @RequestBody RezervacijaRequest rezervacijaRequest) {
        String username = userService.getUserByToken(token);
        return ResponseEntity.ok(transakcijaService.rezervacija(username, UUID.fromString(rezervacijaRequest.getRacun()), rezervacijaRequest.getOpis(), rezervacijaRequest.getValuta(), rezervacijaRequest.getIsplata(),rezervacijaRequest.getRezervacijaKoristi(),rezervacijaRequest.getHartijeOdVrednostiID()));
    }



}
