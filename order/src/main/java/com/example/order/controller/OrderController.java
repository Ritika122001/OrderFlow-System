package com.example.order.controller;

import com.example.order.dto.RequestOrderDTO;
import com.example.order.dto.ResponseOrderDTO;
import com.example.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/createOrders")
    public ResponseOrderDTO createOrder(@RequestBody RequestOrderDTO orderDTO) {
        return orderService.createNewOrder(orderDTO);
    }

    @PutMapping("/orders/{email}")
    public ResponseOrderDTO updateOrder(@PathVariable String email, @RequestBody RequestOrderDTO orderDTO) {
        return orderService.updateOrder(email, orderDTO);
    }

    @GetMapping("/getOrders")
    public List<ResponseOrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseOrderDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}