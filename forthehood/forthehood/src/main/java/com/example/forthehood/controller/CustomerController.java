package com.example.forthehood.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.forthehood.dto.CreateOrderRequest;
import com.example.forthehood.dto.CustomerProfileResponse;
import com.example.forthehood.dto.OrderResponse;
import com.example.forthehood.dto.UpdateCustomerProfileRequest;
import com.example.forthehood.service.CustomerService;
import com.example.forthehood.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class CustomerController {

    private final OrderService orderService;
    private final CustomerService customerService;

    public CustomerController(OrderService orderService,
                              CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/orders/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getOrdersByCurrentCustomer());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getMyOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrderForCurrentCustomer(id));
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

    @GetMapping("/customers/profile")
    public ResponseEntity<CustomerProfileResponse> getMyProfile() {
        return ResponseEntity.ok(customerService.getCurrentCustomerProfile());
    }

    @PutMapping("/customers/profile")
    public ResponseEntity<CustomerProfileResponse> updateMyProfile(@Valid @RequestBody UpdateCustomerProfileRequest request) {
        return ResponseEntity.ok(customerService.updateCurrentCustomerProfile(request));
    }
}
