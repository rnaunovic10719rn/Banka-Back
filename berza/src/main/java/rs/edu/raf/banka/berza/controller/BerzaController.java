package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.OrderDto;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.response.ApproveRejectOrderResponse;
import rs.edu.raf.banka.berza.response.OrderResponse;
import rs.edu.raf.banka.berza.response.OrderStatusResponse;
import rs.edu.raf.banka.berza.service.impl.BerzaService;
import rs.edu.raf.banka.berza.service.impl.OrderService;
import rs.edu.raf.banka.berza.service.impl.UserService;
import rs.edu.raf.banka.berza.utils.MessageUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaService berzaService;
    private final UserService userService;
    private final OrderService orderService;
    private final ModelMapper modelMapper;

    @Autowired
    public BerzaController(BerzaService berzaService,
                           OrderService orderService,
                           UserService userService,
                           ModelMapper modelMapper){
        this.berzaService = berzaService;
        this.orderService = orderService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(berzaService.findAll());
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
        return ResponseEntity.ok(orders.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makeOrder(@RequestHeader("Authorization") String token,
                                       @RequestBody OrderRequest orderRequest){
        OrderResponse resp = berzaService.makeOrder(token, orderRequest.getUserId(), orderRequest.getSymbol(), orderRequest.getHartijaOdVrednostiTip(),
                orderRequest.getKolicina(), orderRequest.getAkcija(),
                orderRequest.getLimitValue(), orderRequest.getStopValue(), orderRequest.isAllOrNoneFlag(), orderRequest.isMarginFlag());
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

    @PostMapping(value = "/order/status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrderStatus(@PathVariable Long id){
        OrderStatusResponse resp = orderService.getOrderStatus(id);
        if(resp == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(resp);
    }

    private OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

}
