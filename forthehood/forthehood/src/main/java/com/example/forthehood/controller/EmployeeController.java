package com.example.forthehood.controller;

import com.example.forthehood.dto.OrderResponse;
import com.example.forthehood.enums.OrderStatus;
import com.example.forthehood.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final OrderService orderService;

    public EmployeeController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable("id") Long id,
                                                           @RequestParam("status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
