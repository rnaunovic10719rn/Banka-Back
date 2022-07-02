package rs.edu.raf.banka.berza.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.requests.AkcijaCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.FuturesCreateUpdateRequest;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.service.impl.*;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import java.util.List;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaService berzaService;
    private final UserService userService;
    private final OrderService orderService;
    private final PriceService priceService;
    private final HartijaService hartijaService;

    @Autowired
    public BerzaController(BerzaService berzaService,
                           OrderService orderService,
                           UserService userService,
                           PriceService priceService,
                           HartijaService hartijaService){
        this.berzaService = berzaService;
        this.orderService = orderService;
        this.userService = userService;
        this.priceService = priceService;
        this.hartijaService = hartijaService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(berzaService.findAll());
    }

    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findBerzaById(@PathVariable Long id){
        return ResponseEntity.ok(berzaService.findBerza(id));
    }

    @GetMapping(value = "/{oznaka}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAkcija(@PathVariable String oznaka){
        return ResponseEntity.ok(berzaService.findAkcije(oznaka));
    }

    @GetMapping(value = "/order/{status}/{done}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String token,
                                       @PathVariable(required = false) String status,
                                       @PathVariable(required = false) Boolean done){
        List<Order> orders = orderService.getOrders(token, status, done);
        return ResponseEntity.ok(orders);
    }

    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String token){
        List<Order> orders = orderService.getOrders(token);
        return ResponseEntity.ok(orders);
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makeOrder(@RequestHeader("Authorization") String token,
                                       @RequestBody OrderRequest orderRequest){
        OrderResponse resp = berzaService.makeOrder(token, orderRequest);
        if(resp.getMessage().equals("Error")) {
            return ResponseEntity.internalServerError().body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/order/approve/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> approveOrder(@RequestHeader("Authorization") String token, @PathVariable Long id){
        ApproveRejectOrderResponse resp = orderService.approveOrder(userService.getUserRoleByToken(token), id);
        if(resp.getMessage().equals(MessageUtils.ORDER_APPROVED)) {
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.internalServerError().body(resp);
    }

    @PostMapping(value = "/order/reject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> rejectOrder(@RequestHeader("Authorization") String token, @PathVariable Long id){
        ApproveRejectOrderResponse resp = orderService.rejectOrder(userService.getUserRoleByToken(token), id);
        if(resp.getMessage().equals(MessageUtils.ORDER_REJECTED)) {
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.internalServerError().body(resp);
    }

    @GetMapping(value = "/hartijeWithSettlementDate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeTimeseries(){
        return ResponseEntity.ok(hartijaService.getAllNearSettlement());
    }

    @GetMapping(value = "/hartija/{hartijaType}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHartija(@PathVariable String hartijaType, @PathVariable long id) {
        return ResponseEntity.ok(hartijaService.findHartijaByIdAndType(id, hartijaType));
    }

    @GetMapping(value = "/askbid/{hartijaType}/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAskBidPrice(@PathVariable String hartijaType, @PathVariable String symbol) {
        AskBidPriceDto dto = priceService.getAskBidPrice(HartijaOdVrednostiType.valueOf(hartijaType.toUpperCase()), symbol);
        if(dto == null || dto.getHartijaId() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/askbid/{hartijaType}/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAskBidPriceByID(@PathVariable String hartijaType, @PathVariable Long id) {
        AskBidPriceDto dto = priceService.getAskBidPrice(HartijaOdVrednostiType.valueOf(hartijaType.toUpperCase()), id);
        if(dto == null || dto.getHartijaId() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/hartija/akcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllStocks(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(berzaService.findAllAkcije());
    }

    @PostMapping(value = "/hartija/akcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCustomStock(@RequestHeader("Authorization") String token, @RequestBody AkcijaCreateUpdateRequest request){
        if(request.getId() != null) {
            return ResponseEntity.badRequest().build();
        }

        Akcije akcija = berzaService.createUpdateAkcija(request);
        if(akcija == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(akcija);
    }

    @PutMapping(value = "/hartija/akcija", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCustomStock(@RequestHeader("Authorization") String token, @RequestBody AkcijaCreateUpdateRequest request){
        if(request.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Akcije akcija = berzaService.createUpdateAkcija(request);
        if(akcija == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(akcija);
    }

    @GetMapping(value = "/hartija/future", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFuturesUgoovri(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(berzaService.findAllFuturesUgovori());
    }

    @PostMapping(value = "/hartija/future", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCustomFuturesContract(@RequestHeader("Authorization") String token, @RequestBody FuturesCreateUpdateRequest request){
        if(request.getId() != null) {
            return ResponseEntity.badRequest().build();
        }

        FuturesUgovori futuresUgovor = berzaService.createUpdateFuturesUgovor(request);
        if(futuresUgovor == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(futuresUgovor);
    }

    @PutMapping(value = "/hartija/future", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCustomFuturesContract(@RequestHeader("Authorization") String token, @RequestBody FuturesCreateUpdateRequest request){
        if(request.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        FuturesUgovori futuresUgovor = berzaService.createUpdateFuturesUgovor(request);
        if(futuresUgovor == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(futuresUgovor);
    }

}
