package com.example.forthehood.controller;

import com.example.forthehood.dto.CreateOrderRequest;
import com.example.forthehood.dto.OrderResponse;
import com.example.forthehood.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CustomerController {

    private final OrderService orderService;

    public CustomerController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/orders/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getOrdersByCurrentCustomer());
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> updateMyOrder(@PathVariable("id") Long id,
                                                       @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrderByCustomer(id, request));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelMyOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.cancelOrderByCustomer(id));
    }
}
