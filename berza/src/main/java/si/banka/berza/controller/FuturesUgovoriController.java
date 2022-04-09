package si.banka.berza.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.banka.berza.model.FuturesUgovori;

@RestController
@RequestMapping("/futures")
public class FuturesUgovoriController {

    public ResponseEntity<FuturesUgovori> getFuturesUgovor() {
        return null;
    }

}
