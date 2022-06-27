package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.requests.*;
import rs.edu.raf.banka.racun.service.impl.UgovorService;

import java.util.List;

@RestController
@RequestMapping("/api/ugovor")
public class UgovorController {
    private final UgovorService ugovorService;

    @Autowired
    public UgovorController(UgovorService ugovorService) {
        this.ugovorService = ugovorService;
    }

    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUgovor(@RequestHeader("Authorization") String token, @PathVariable Long id) throws Exception {
        var ugovor = ugovorService.getUgovorById(id, token);
        return ResponseEntity.ok(ugovor);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) throws Exception {
        var ugovori = ugovorService.getAll(token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/finalized/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFinalized(@RequestHeader("Authorization") String token) throws Exception {
        var ugovori = ugovorService.getAllFinalized(token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/draft/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllDraft(@RequestHeader("Authorization") String token) throws Exception {
        var ugovori = ugovorService.getAllDraft(token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanija(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) throws Exception {
        List<Ugovor> ugovori = ugovorService.getAllByCompany(kompanijaId, token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}/finalized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanijaFinalized(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) throws Exception {
        var ugovori = ugovorService.getAllByCompanyAndUgovorStatus(kompanijaId, token, UgovorStatus.FINALIZED);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanijaDraft(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) throws Exception {
        var ugovori = ugovorService.getAllByCompanyAndUgovorStatus(kompanijaId, token, UgovorStatus.DRAFT);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/delovodnibroj/{delovodniBroj}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllBrUgovora(@RequestHeader("Authorization") String token, @PathVariable String delovodniBroj) throws Exception {
        List<Ugovor> ugovori = ugovorService.getAllByDelovodniBroj(delovodniBroj, token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/delovodnibroj/{delovodniBroj}/finalized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllBrUgovoraFinalized(@RequestHeader("Authorization") String token, @PathVariable String delovodniBroj) throws Exception {
        var ugovori  = ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.FINALIZED);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/delovodnibroj/{delovodniBroj}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllBrUgovoraDraft(@RequestHeader("Authorization") String token, @PathVariable String delovodniBroj) throws Exception {
        var ugovori = ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.DRAFT);
        return ResponseEntity.ok(ugovori);
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorCreateRequest request) throws Exception {
        var ugovor = ugovorService.createUgovor(request, token);
        return ResponseEntity.ok(ugovor);
    }

    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorUpdateRequest request) throws Exception {
        var result = ugovorService.modifyUgovor(request, token);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/finalize/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovorDocument(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
        var result = ugovorService.finalizeUgovor(id, file, token);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getContractDocument(@RequestHeader("Authorization") String token, @PathVariable Long id) throws Exception {
        var result = ugovorService.getContractDocument(id, token);
        return result.getData();
    }

    @PostMapping(value = "/stavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaRequest request) throws Exception {
        var stavka = ugovorService.addStavka(request, token);
        return ResponseEntity.ok(stavka);
    }

    @GetMapping(value = "/stavka/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStavka(@RequestHeader("Authorization") String token, @PathVariable Long id) throws Exception {
        var stavka = ugovorService.getTransakcionaStavkaById(id, token);
        return ResponseEntity.ok(stavka);
    }

    @PutMapping(value = "/stavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaRequest request) throws Exception {
        var result = ugovorService.modifyStavka(request, token);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(value = "/stavka/{stavkaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeStavka(@RequestHeader("Authorization") String token, @PathVariable Long stavkaId) throws Exception {
        var result = ugovorService.removeStavka(stavkaId, token);
        return ResponseEntity.ok(result);
    }
}
