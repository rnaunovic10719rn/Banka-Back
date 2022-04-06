package si.banka.berza.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.berza.requests.BuyHartijaOdVrednostiRequest;
import si.banka.berza.requests.SellHartijaOdVrednostiRequest;
import si.banka.berza.service.impl.BerzaServiceImplementation;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaServiceImplementation berzaServiceImplementation;


    public BerzaController(BerzaServiceImplementation berzaServiceImplementation){
        this.berzaServiceImplementation = berzaServiceImplementation;
    }

    @PostMapping(value = "/buy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> buy(@RequestBody BuyHartijaOdVrednostiRequest buyHartijaOdVrednostiRequest){

        //response priblizna cena kolicina * cena
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/sell", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sell(@RequestBody SellHartijaOdVrednostiRequest sellHartijaOdVrednostiRequest){
        return ResponseEntity.ok().build();
    }


    //da li je berza zatvorena ili u after-hours stanju (proslo manje od 4h po zatvaranju berze)
    //TODO: vrati svoj response
    @GetMapping(value = "/order-status/{id_berza}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrderStatus(@PathVariable Long id_berza){
        return ResponseEntity.ok(berzaServiceImplementation.getOrderStatus(id_berza));
    }

}
