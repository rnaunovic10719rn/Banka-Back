package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.dto.DateFilter;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UserService;
import org.modelmapper.ModelMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/racun")
public class RacunController {


    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaService transakcijaService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public RacunController(SredstvaKapitalService sredstvaKapitalService, TransakcijaService transakcijaService, UserService userService, ModelMapper modelMapper) {
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaService = transakcijaService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }


    @PostMapping(value = "/transakcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajTransakciju(@RequestHeader("Authorization") String token, @RequestBody TransakcijaRequest transakcijaRequest) {
        if(transakcijaRequest.getOrderId() == null && transakcijaRequest.getRezervisano() > 0) {
            return ResponseEntity.badRequest().body("bad request");
        }
        Transakcija t = transakcijaService.dodajTransakciju(token, transakcijaRequest.getBrojRacuna()
                , transakcijaRequest.getOpis(), transakcijaRequest.getValutaOznaka()
                , transakcijaRequest.getOrderId(), transakcijaRequest.getUplata()
                , transakcijaRequest.getIsplata(), transakcijaRequest.getRezervisano()
                , transakcijaRequest.getLastSegment()
                , transakcijaRequest.getType(), transakcijaRequest.getHartijaId());
        if (t == null) {
            return ResponseEntity.badRequest().body("bad request");
        }
        return ResponseEntity.ok(t);
    }

    @GetMapping(value = "/transakcije", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcije(@RequestHeader("Authorization") String token, @RequestBody(required = false) DateFilter filter) {
        if(filter == null || filter.from == null || filter.to == null)
            return ResponseEntity.ok(transakcijaService.getAll(token)); //Pregled svojih transakcija
        return ResponseEntity.ok(transakcijaService.getAll(token, filter.from, filter.to));
    }

    @GetMapping(value = "/transakcije/{valuta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcijeValuta(@RequestHeader("Authorization") String token, @PathVariable String valuta, @RequestBody(required = false) DateFilter filter) {
        if(filter == null || filter.from == null || filter.to == null)
            return ResponseEntity.ok(transakcijaService.getAll(token, valuta));
        return ResponseEntity.ok(transakcijaService.getAll(token, valuta, filter.from, filter.to));
    }

    @GetMapping(value = "/stanjeSupervisor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanjeSupervisor(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(sredstvaKapitalService.findSredstvaKapitalSupervisor(token));


    }
    @GetMapping(value = "/stanje/{racun}/{hartijaType}/{hartijaId}/{valuta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SredstvaKapital> getStanjeHartija(@RequestHeader("Authorization") String token, @PathVariable String racun, @PathVariable String hartijaType, @PathVariable Long hartijaId, @PathVariable String valuta) {
         /*
               TODO Porvera da li je supervizor
            */
        return ResponseEntity.ok(sredstvaKapitalService.get(UUID.fromString(racun), valuta, KapitalType.valueOf(hartijaType), hartijaId));
    }

    @GetMapping(value = "/stanjeAgent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanjeAgent(@RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(sredstvaKapitalService.findSredstvaKapitalAgent(token));
    }

    @GetMapping(value = "/akcija/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanjeAgent(@PathVariable Long id) {

        return sredstvaKapitalService.getAkcija(id);
    }


//    @GetMapping(value = "/stanje/{racun}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> getStanje(@RequestHeader("Authorization") String token, @PathVariable String racun) {
//        String role = userService.getRoleByToken(token);
//        if (role.equals("ROLE_AGENT")) {
//            return ResponseEntity.badRequest().body("bad request");
//        }
//        var kapitali = sredstvaKapitalService.getAll(UUID.fromString(racun));
//        var result = sredstvaKapitalService.getSumStanje(kapitali, token);
//        return ResponseEntity.ok(result);
//
//    }


}
