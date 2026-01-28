package com.example.forthehood.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryCreateUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
