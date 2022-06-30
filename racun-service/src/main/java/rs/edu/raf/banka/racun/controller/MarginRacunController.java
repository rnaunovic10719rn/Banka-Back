package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.dto.*;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.requests.MarginTransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.MarginTransakcijaService;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;

import java.util.List;

@RestController
@RequestMapping("/api/margin")
public class MarginRacunController {

    private final MarginTransakcijaService marginTransakcijaService;
    private final SredstvaKapitalService sredstvaKapitalService;

    @Autowired
    public MarginRacunController(MarginTransakcijaService marginTransakcijaService,
                                    SredstvaKapitalService sredstvaKapitalService) {
        this.marginTransakcijaService = marginTransakcijaService;
        this.sredstvaKapitalService = sredstvaKapitalService;
    }

    @PostMapping(value = "/transakcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajTransakciju(@RequestHeader("Authorization") String token, @RequestBody MarginTransakcijaRequest request) {
        MarginTransakcija mt = marginTransakcijaService.dodajTransakciju(token, request);
        if (mt == null) {
            return ResponseEntity.badRequest().body("bad request");
        }
        return ResponseEntity.ok(mt);
    }

    @GetMapping(value = "/transakcije", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcije(@RequestHeader("Authorization") String token, @RequestBody(required = false) DateFilter filter) {
        if(filter == null || filter.from == null || filter.to == null)
            return ResponseEntity.ok(marginTransakcijaService.getAll(token)); //Pregled svojih transakcija
        return ResponseEntity.ok(marginTransakcijaService.getAll(token, filter.from, filter.to));
    }

    @GetMapping(value = "/stanje", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSredstvaStanje(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(sredstvaKapitalService.findSredstvaKapitalSupervisor(token, true));
    }

    @GetMapping(value = "/kapitalStanje", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getKapitalStanje(@RequestHeader("Authorization") String token) {
        List<KapitalHartijeDto> kapitalHartijeDtoList = sredstvaKapitalService.getUkupnoStanjePoHartijama(token, true);
        if (kapitalHartijeDtoList == null)
            return ResponseEntity.badRequest().body("bad request");
        return ResponseEntity.ok(kapitalHartijeDtoList);
    }

    @GetMapping(value = "/kapitalStanje/{kapitalType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanjePoTipu(@RequestHeader("Authorization") String token, @PathVariable String kapitalType) {
        // TODO: Ispraviti za margine
        List<KapitalPoTipuHartijeDto> kapitalPoTipuHartijeDtos = sredstvaKapitalService.getStanjeJednogTipaHartije(token, kapitalType, true);
        if (kapitalPoTipuHartijeDtos == null)
            return ResponseEntity.badRequest().body("bad request");
        return ResponseEntity.ok(kapitalPoTipuHartijeDtos);
    }

    @GetMapping(value = "/transakcijaHartije/{kapitalType}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcijeHartije(@RequestHeader("Authorization") String token, @PathVariable Long id, @PathVariable String kapitalType) {
        List<MarginTransakcijeHartijeDto> transakcijeHartijeDtos = sredstvaKapitalService.getTransakcijeHartijeMargins(id, kapitalType);
        if (transakcijeHartijeDtos == null)
            return ResponseEntity.badRequest().body("bad request");
        return ResponseEntity.ok(transakcijeHartijeDtos);
    }

}
