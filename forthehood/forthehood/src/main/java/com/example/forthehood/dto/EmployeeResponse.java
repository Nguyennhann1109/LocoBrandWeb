package com.example.forthehood.dto;

public class EmployeeResponse {
    private Long id;
    private String email;
    private String name;
    private String position;

    public EmployeeResponse(Long id, String email, String name, String position) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
