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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaService berzaService;

    private final OrderService orderService;

    @Autowired
    private ModelMapper modelMapper;


    public BerzaController(BerzaService berzaService, OrderService orderService){
        this.berzaService = berzaService;
        this.orderService = orderService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(berzaService.findAll());
    }

    @GetMapping(value = "/{s}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAkcija(@PathVariable String s){
        return ResponseEntity.ok(berzaService.findAkcije(s));
    }

    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrders(){
        List<Order> orders = orderService.getOrders();
        return ResponseEntity.ok(orders.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makeOrder(@RequestBody OrderRequest orderRequest){
        return ResponseEntity.ok(berzaService.makeOrder(orderRequest.getUserId(), orderRequest.getSymbol(), orderRequest.getHartijaOdVrednostiTip(),
                orderRequest.getKolicina(), orderRequest.getAkcija(),
                orderRequest.getLimitValue(), orderRequest.getStopValue(), orderRequest.isAllOrNoneFlag(), orderRequest.isMarginFlag()));
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
