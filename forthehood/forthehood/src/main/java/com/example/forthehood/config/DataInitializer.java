package com.example.forthehood.config;

import com.example.forthehood.entity.Account;
import com.example.forthehood.entity.Role;
import com.example.forthehood.enums.AccountStatus;
import com.example.forthehood.enums.RoleName;
import com.example.forthehood.repository.AccountRepository;
import com.example.forthehood.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedData(RoleRepository roleRepository,
                                      AccountRepository accountRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed roles if not exist
            for (RoleName roleName : RoleName.values()) {
                roleRepository.findByName(roleName).orElseGet(() -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .build();
                    return roleRepository.save(role);
                });
            }

            // Admin account
            if (accountRepository.findByEmail("admin").isEmpty()) {
                Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
                Account admin = Account.builder()
                        .email("admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .status(AccountStatus.ACTIVE)
                        .role(adminRole)
                        .createdAt(LocalDateTime.now())
                        .build();
                accountRepository.save(admin);
            }

            // Employee account
            if (accountRepository.findByEmail("employee").isEmpty()) {
                Role empRole = roleRepository.findByName(RoleName.EMPLOYEE).orElseThrow();
                Account employee = Account.builder()
                        .email("employee")
                        .password(passwordEncoder.encode("Employee@123"))
                        .status(AccountStatus.ACTIVE)
                        .role(empRole)
                        .createdAt(LocalDateTime.now())
                        .build();
                accountRepository.save(employee);
            }

            // Customer account
            if (accountRepository.findByEmail("customer0").isEmpty()) {
                Role customerRole = roleRepository.findByName(RoleName.CUSTOMER).orElseThrow();
                Account customer = Account.builder()
                        .email("customer0")
                        .password(passwordEncoder.encode("Customer0@123"))
                        .status(AccountStatus.ACTIVE)
                        .role(customerRole)
                        .createdAt(LocalDateTime.now())
                        .build();
                accountRepository.save(customer);
            }
        };
    }
}
