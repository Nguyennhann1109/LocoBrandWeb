package com.example.forthehood.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.forthehood.dto.ChangePasswordRequest;
import com.example.forthehood.dto.LoginRequest;
import com.example.forthehood.dto.LoginResponse;
import com.example.forthehood.dto.RegisterCustomerRequest;
import com.example.forthehood.dto.RegisterCustomerResponse;
import com.example.forthehood.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        RegisterCustomerResponse response = authService.registerCustomer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
