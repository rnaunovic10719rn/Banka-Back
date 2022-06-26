package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.dto.KapitalHartijeDto;
import rs.edu.raf.banka.racun.dto.KapitalPoTipuHartijeDto;
import rs.edu.raf.banka.racun.dto.TransakcijeHartijeDto;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.dto.DateFilter;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.requests.ValutaService;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UserService;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/racun")
public class RacunController {


    private final SredstvaKapitalService sredstvaKapitalService;
    private final TransakcijaService transakcijaService;
    private final ValutaService valutaService;


    @Autowired
    public RacunController(SredstvaKapitalService sredstvaKapitalService, TransakcijaService transakcijaService, ValutaService valutaService) {
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.transakcijaService = transakcijaService;
        this.valutaService = valutaService;
    }

    @GetMapping(value = "/valute", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getValute() {
        return ResponseEntity.ok(valutaService.getValute());
    }

    @PostMapping(value = "/transakcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dodajTransakciju(@RequestHeader("Authorization") String token, @RequestBody TransakcijaRequest transakcijaRequest) {
        if(transakcijaRequest.getOrderId() == null && transakcijaRequest.getRezervisano() > 0) {
            return ResponseEntity.badRequest().body("bad request");
        }
        Transakcija t = transakcijaService.dodajTransakciju(token, transakcijaRequest);
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
    @GetMapping(value = "/stanje/{racun}/{hartijaType}/{hartijaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SredstvaKapital> getStanjeHartija(@RequestHeader("Authorization") String token, @PathVariable String racun, @PathVariable String hartijaType, @PathVariable Long hartijaId) {
         /*
               TODO Porvera da li je supervizor
            */
        return ResponseEntity.ok(sredstvaKapitalService.get(UUID.fromString(racun), KapitalType.valueOf(hartijaType), hartijaId));
    }

    @GetMapping(value = "/stanjeAgent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanjeAgent(@RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(sredstvaKapitalService.findSredstvaKapitalAgent(token));
    }


    @GetMapping(value = "/kapitalStanje", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanje(@RequestHeader("Authorization") String token) {
        List<KapitalHartijeDto> kapitalHartijeDtoList = sredstvaKapitalService.getUkupnoStanjePoHartijama(token);
        if (kapitalHartijeDtoList == null)
            return ResponseEntity.badRequest().body("bad request");
        return ResponseEntity.ok(kapitalHartijeDtoList);
    }

    @GetMapping(value = "/kapitalStanje/{kapitalType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStanjePoTipu(@RequestHeader("Authorization") String token, @PathVariable String kapitalType) {
        List<KapitalPoTipuHartijeDto> kapitalPoTipuHartijeDtos = sredstvaKapitalService.getStanjeJednogTipaHartije(token, kapitalType);
        if (kapitalPoTipuHartijeDtos == null)
            return ResponseEntity.badRequest().body("bad request");
        return ResponseEntity.ok(kapitalPoTipuHartijeDtos);
    }

    @GetMapping(value = "/transakcijaHartije/{kapitalType}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTransakcijeHartije(@RequestHeader("Authorization") String token, @PathVariable Long id, @PathVariable String kapitalType) {
        List<TransakcijeHartijeDto> transakcijeHartijeDtos = sredstvaKapitalService.getTransakcijeHartije(id, kapitalType);
        if (transakcijeHartijeDtos == null)
            return ResponseEntity.badRequest().body("bad request");
        return ResponseEntity.ok(transakcijeHartijeDtos);
    }

}
