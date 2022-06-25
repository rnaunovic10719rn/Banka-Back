package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.requests.*;
import rs.edu.raf.banka.racun.service.impl.UgovorService;

import java.util.List;

@RestController
@RequestMapping("/api/ugovor")
public class UgovorController
{
    private final UgovorService ugovorService;

    @Autowired
    public UgovorController(UgovorService ugovorService) {
        this.ugovorService = ugovorService;
    }

    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUgovor(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        var ugovor = ugovorService.getById(id);
        if(ugovor == null)
            return ResponseEntity.badRequest().body("Ugovor not found");

        return ResponseEntity.ok(ugovor);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAll();
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/finalized/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFinalized(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAllFinalized();
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/draft/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllDraft(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAllDraft();
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanija(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        try {
            List<Ugovor> ugovori = ugovorService.getAllByCompany(kompanijaId);
            return ResponseEntity.ok(ugovori);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping(value = "/company/{kompanijaId}/finalized", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @GetMapping(value = "/company/{kompanijaId}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorCreateRequest request) {
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

    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorUpdateRequest request) {
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

    @PostMapping(value = "/finalize/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovorDocument(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            var result = ugovorService.finalizeUgovor(id, file);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getContractDocument(@RequestHeader("Authorization") String token, @PathVariable Long id) throws Exception {
        var result = ugovorService.getContractDocument(id);
        return result.getData();
    }

    @PostMapping(value = "/stavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaCreateRequest request) {
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

    @PutMapping(value = "/stavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaUpdateRequest request) {
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

    @DeleteMapping(value = "/stavka/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
