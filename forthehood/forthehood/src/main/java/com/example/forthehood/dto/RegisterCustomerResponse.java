package com.example.forthehood.dto;

public class RegisterCustomerResponse {
    private Long customerId;
    private String email;

    public RegisterCustomerResponse(Long customerId, String email) {
        this.customerId = customerId;
        this.email = email;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
