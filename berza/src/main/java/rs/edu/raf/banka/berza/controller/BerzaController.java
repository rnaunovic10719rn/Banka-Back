package rs.edu.raf.banka.berza.controller;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.OrderDto;
import rs.edu.raf.banka.berza.model.Order;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.service.impl.BerzaService;
import rs.edu.raf.banka.berza.service.impl.OrderService;
import rs.edu.raf.banka.berza.service.impl.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaService berzaService;
    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    private ModelMapper modelMapper;


    public BerzaController(BerzaService berzaService, OrderService orderService, UserService userService){
        this.berzaService = berzaService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(berzaService.findAll());
    }

    @GetMapping(value = "/{s}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAkcija(@PathVariable String s){
        return ResponseEntity.ok(berzaService.findAkcije(s));
    }

    @GetMapping(value = "/order/{status}/{done}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrders(@PathVariable(required = false) String status,
                                       @PathVariable(required = false) Boolean done){
        List<Order> orders = orderService.getOrders(status, done);
        return ResponseEntity.ok(orders.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    //getUser by token
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(userService.getUserByToken(token));
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makeOrder(@RequestHeader("Authorization") String token,
                                       @RequestBody OrderRequest orderRequest){
        return ResponseEntity.ok(berzaService.makeOrder(token, orderRequest.getUserId(), orderRequest.getSymbol(), orderRequest.getHartijaOdVrednostiTip(),
                orderRequest.getKolicina(), orderRequest.getAkcija(),
                orderRequest.getLimitValue(), orderRequest.getStopValue(), orderRequest.isAllOrNoneFlag(), orderRequest.isMarginFlag()));
    }

    @GetMapping(value = "/order/execute/{id}/{berzaId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> executeOrder(@PathVariable Long id, @PathVariable Long berzaId) {
        return ResponseEntity.ok(berzaService.executeOrder(id, berzaId));
    }

    @GetMapping(value = "/order/approve/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> approveOrder(@RequestHeader("Authorization") String token, @PathVariable Long id){
        return ResponseEntity.ok(orderService.approveOrder(userService.getUserRoleByToken(token), id));
    }

    @GetMapping(value = "/order/reject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> rejectOrder(@RequestHeader("Authorization") String token, @PathVariable Long id){
        return ResponseEntity.ok(orderService.rejectOrder(userService.getUserRoleByToken(token), id));
    }

    @GetMapping(value = "/order-status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrderStatus(@PathVariable Long id){
        return ResponseEntity.ok(berzaService.getOrderStatus(id));
    }

    private OrderDto convertToDto(Order order) {
        OrderDto machineDto = modelMapper.map(order, OrderDto.class);
        return machineDto;
    }

}
