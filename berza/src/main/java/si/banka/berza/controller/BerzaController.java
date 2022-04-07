package si.banka.berza.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.berza.requests.BuyHartijaOdVrednostiRequest;
import si.banka.berza.requests.OrderRequest;
import si.banka.berza.requests.SellHartijaOdVrednostiRequest;
import si.banka.berza.service.impl.BerzaServiceImplementation;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaServiceImplementation berzaServiceImplementation;


    public BerzaController(BerzaServiceImplementation berzaServiceImplementation){
        this.berzaServiceImplementation = berzaServiceImplementation;
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makeOrder(@RequestBody OrderRequest orderRequest){
        berzaServiceImplementation.makeOrder(orderRequest.getBerza_id() , orderRequest.getUser_id(), orderRequest.getHartija_od_vrednosti_id(), orderRequest.getHartija_od_vrednosti_tip(),
                orderRequest.getKolicina(), orderRequest.getAkcija(), orderRequest.getOrder_tip());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/order-status/{id_berza}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrderStatus(@PathVariable Long id_berza){
        return ResponseEntity.ok(berzaServiceImplementation.getOrderStatus(id_berza));
    }

}
