package si.banka.berza.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.banka.berza.model.FuturesUgovori;

@RestController
@RequestMapping("/futures")
public class FuturesUgovoriController {

    public ResponseEntity<FuturesUgovori> GetFuturesUgovor() {
        return null;
    }

}
