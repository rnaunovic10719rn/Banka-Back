package racun.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import racun.requests.UplataRequest;
import racun.service.impl.RacunService;
import racun.service.impl.SredstvaKapitalService;
import racun.service.impl.TransakcijaService;
import racun.service.impl.UserService;

@RestController
@RequestMapping("/api/racun")
public class RacunController {

    private final RacunService racunService;
    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaService transakcijaService;
    private final UserService userService;

    public RacunController(RacunService racunService, SredstvaKapitalService sredstvaKapitalService, TransakcijaService transakcijaService, UserService userService) {
        this.racunService = racunService;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaService = transakcijaService;
        this.userService = userService;
    }


    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajRacun(@RequestHeader("Authorization") String token,@PathVariable Long id) {
        String user = userService.getUserByToken(token); //Read id from token
        return ResponseEntity.ok(racunService.createRacun(id));
    }

    @PostMapping(value = "/uplata", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajUplatu(@RequestHeader("Authorization") String token,@RequestBody UplataRequest uplataRequest) {
        String user = userService.getUserByToken(token); //Read id from token
        return ResponseEntity.ok(transakcijaService.uplata(uplataRequest.getUserid(), uplataRequest.getBrojRacuna(), uplataRequest.getOpis(), uplataRequest.getValuta(), uplataRequest.getUplata()));
    }

    @PostMapping(value = "/isplata", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajIsplatu(@RequestHeader("Authorization") String token,@RequestBody UplataRequest uplataRequest) {
        String user = userService.getUserByToken(token); //Read id from token
        return ResponseEntity.ok(transakcijaService.isplata(uplataRequest.getUserid(), uplataRequest.getBrojRacuna(), uplataRequest.getOpis(), uplataRequest.getValuta(), uplataRequest.getUplata()));
    }


    @GetMapping(value = "/transakcije", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcije(@RequestHeader("Authorization") String token) {
        String user = userService.getUserByToken(token);
        return ResponseEntity.ok(transakcijaService.getAll(1)); //Read id from token
    }

    @GetMapping (value = "/stanje/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanje(@RequestHeader("Authorization") String token,@PathVariable Long id){
        String user = userService.getUserByToken(token); //Provera da li je supervizor
        return ResponseEntity.ok(sredstvaKapitalService.getAll(id));
    }



}
