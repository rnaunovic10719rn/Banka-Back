package rs.edu.raf.banka.racun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.enums.UgovorStatus;
import rs.edu.raf.banka.racun.exceptions.ContractExpcetion;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.requests.TransakcionaStavkaRequest;
import rs.edu.raf.banka.racun.requests.UgovorCreateRequest;
import rs.edu.raf.banka.racun.requests.UgovorUpdateRequest;
import rs.edu.raf.banka.racun.service.impl.UgovorService;

import java.io.IOException;
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
    public ResponseEntity<?> getUgovor(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        var ugovor = ugovorService.getUgovorById(id, token);
        return ResponseEntity.ok(ugovor);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAll(token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/finalized/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFinalized(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAllFinalized(token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/draft/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllDraft(@RequestHeader("Authorization") String token) {
        var ugovori = ugovorService.getAllDraft(token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanija(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        List<Ugovor> ugovori = ugovorService.getAllByCompany(kompanijaId, token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}/finalized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanijaFinalized(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        var ugovori = ugovorService.getAllByCompanyAndUgovorStatus(kompanijaId, token, UgovorStatus.FINALIZED);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/company/{kompanijaId}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllKompanijaDraft(@RequestHeader("Authorization") String token, @PathVariable Long kompanijaId) {
        var ugovori = ugovorService.getAllByCompanyAndUgovorStatus(kompanijaId, token, UgovorStatus.DRAFT);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/delovodnibroj/{delovodniBroj}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllBrUgovora(@RequestHeader("Authorization") String token, @PathVariable String delovodniBroj) {
        List<Ugovor> ugovori = ugovorService.getAllByDelovodniBroj(delovodniBroj, token);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/delovodnibroj/{delovodniBroj}/finalized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllBrUgovoraFinalized(@RequestHeader("Authorization") String token, @PathVariable String delovodniBroj) {
        var ugovori  = ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.FINALIZED);
        return ResponseEntity.ok(ugovori);
    }

    @GetMapping(value = "/delovodnibroj/{delovodniBroj}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllBrUgovoraDraft(@RequestHeader("Authorization") String token, @PathVariable String delovodniBroj) {
        var ugovori = ugovorService.getAllByDelovodniBrojAndUgovorStatus(delovodniBroj, token, UgovorStatus.DRAFT);
        return ResponseEntity.ok(ugovori);
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorCreateRequest request) {
        var ugovor = ugovorService.createUgovor(request, token);
        return ResponseEntity.ok(ugovor);
    }

    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyUgovor(@RequestHeader("Authorization") String token, @RequestBody UgovorUpdateRequest request) {
        var result = ugovorService.modifyUgovor(request, token);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/finalize/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> finalizeUgovor(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestParam("file") MultipartFile file) throws ContractExpcetion, IOException {
        var result = ugovorService.finalizeUgovor(id, file, token);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/reject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> rejectUgovor(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        var result = ugovorService.rejectUgovor(id, token);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getContractDocument(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        var result = ugovorService.getContractDocument(id, token);
        return result.getData();
    }

    @PostMapping(value = "/stavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaRequest request) {
        var stavka = ugovorService.addStavka(request, token);
        return ResponseEntity.ok(stavka);
    }

    @GetMapping(value = "/stavka/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStavka(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        var stavka = ugovorService.getTransakcionaStavkaById(id, token);
        return ResponseEntity.ok(stavka);
    }

    @PutMapping(value = "/stavka", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyStavka(@RequestHeader("Authorization") String token, @RequestBody TransakcionaStavkaRequest request) {
        var result = ugovorService.modifyStavka(request, token);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(value = "/stavka/{stavkaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeStavka(@RequestHeader("Authorization") String token, @PathVariable Long stavkaId) {
        var result = ugovorService.removeStavka(stavkaId, token);
        return ResponseEntity.ok(result);
    }
}
