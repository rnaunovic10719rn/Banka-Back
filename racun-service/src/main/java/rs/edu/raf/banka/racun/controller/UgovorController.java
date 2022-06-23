package rs.edu.raf.banka.racun.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.model.Ugovor;
import rs.edu.raf.banka.racun.requests.*;
import rs.edu.raf.banka.racun.service.impl.UgovorService;

import java.util.List;

@RestController
@RequestMapping("/api/ugovor")
public class UgovorController
{
    private final UgovorService ugovorService;

    public UgovorController(UgovorService ugovorService) {
        this.ugovorService = ugovorService;
    }

    @GetMapping(value = "/get/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUgovor(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        var ugovor = ugovorService.getById(id);
        if(ugovor == null)
            return ResponseEntity.badRequest().body("Ugovor not found");

        return ResponseEntity.ok(ugovor);
    }

    @GetMapping(value = "/get/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAll();
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/get/finalized/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFinalized(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAllFinalized();
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/get/draft/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllDraft(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAllDraft();
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/get/company/{kompanijaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanija(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        try {
            List<Ugovor> ugovori = ugovorService.getAllByCompany(kompanijaId);
            return ResponseEntity.ok(ugovori);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping(value = "/get/company/{kompanijaId}/finalized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanijaFinalized(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        try
        {
            var ugovori = ugovorService.getAllByCompanyFinalized(kompanijaId);
            return ResponseEntity.ok(ugovori);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping(value = "/get/company/{kompanijaId}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanijaDraft(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        try
        {
            var ugovori = ugovorService.getAllByCompanyDraft(kompanijaId);
            return ResponseEntity.ok(ugovori);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorCreateRequest request) {
        if(request.getCompanyId() == null || request.getDescription() == null || request.getDelodavniBroj() == null)
            return ResponseEntity.badRequest().body("bad request");

        try
        {
            var ugovor = ugovorService.createUgovor(request);
            return ResponseEntity.ok(ugovor);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorUpdateRequest request) {
        if(request.getCompanyId() == null && request.getDescription() == null && request.getDelodavniBroj() == null)
            return ResponseEntity.badRequest().body("bad request");
        try
        {
            var result = ugovorService.modifyUgovor(request);
            return ResponseEntity.ok(result);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping(value = "/modify/{id}/addDocument", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovorDocument(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file == null)
            return ResponseEntity.badRequest().body("bad request");
        try {
            var result = ugovorService.modifyDocument(id, file);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping(value = "/modify/addStavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaCreateRequest request) {
        if(request.getUgovorId() == null || request.getCenaHartije() == null || request.getHartijaId() == null
                || request.getKolicina() == null || request.getHartijaType() == null || request.getType() == null
                || request.getRacunId() == null || request.getValuta() == null)
            return ResponseEntity.badRequest().body("bad request");
        try
        {
            var stavka = ugovorService.addStavka(request);
            return ResponseEntity.ok(stavka);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @PostMapping(value = "/modify/modifyStavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaUpdateRequest request) {
        if(request.getStavkaId() == null && request.getCenaHartije() == null && request.getHartijaId() == null
                && request.getKolicina() == null && request.getHartijaType() == null && request.getType() == null
                && request.getRacunId() == null && request.getValuta() == null)
            return ResponseEntity.badRequest().body("bad request");

        try
        {
            var result = ugovorService.modifyStavka(request);
            return ResponseEntity.ok(result);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping(value = "/modify/removeStavka/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeStavka(@RequestHeader("Authorization") String token, @PathVariable Long stavkaId) {
        try
        {
            var result = ugovorService.removeStavka(stavkaId);
            return ResponseEntity.ok(result);
        }
        catch (Exception ex)
        {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
