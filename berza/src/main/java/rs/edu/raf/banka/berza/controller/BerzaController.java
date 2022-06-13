package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.service.impl.BerzaService;
import rs.edu.raf.banka.berza.service.impl.HartijaService;
import rs.edu.raf.banka.berza.service.impl.OrderService;
import rs.edu.raf.banka.berza.service.impl.UserService;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import java.util.List;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaService berzaService;
    private final UserService userService;
    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private HartijaService hartijaService;

    @Autowired
    public BerzaController(BerzaService berzaService,
                           OrderService orderService,
                           UserService userService,
                           ModelMapper modelMapper,
                           HartijaService hartijaService){
        this.berzaService = berzaService;
        this.orderService = orderService;
        this.userService = userService;
        this.modelMapper = modelMapper;
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
    public ResponseEntity<?> getHartija(@PathVariable String hartijaType,  @PathVariable long id){
        return ResponseEntity.ok(hartijaService.findHartijaByIdAndType(id, hartijaType));
    }

}
