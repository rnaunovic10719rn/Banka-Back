package rs.edu.raf.banka.racun.controller;

import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.racun.service.impl.MarzniRacunService;

@RestController
@RequestMapping("/api/marzniRacun")
public class MarzniRacunController {

    private final MarzniRacunService marzniRacunService;

    public MarzniRacunController(MarzniRacunService marzniRacunService) {
        this.marzniRacunService = marzniRacunService;
    }

}
