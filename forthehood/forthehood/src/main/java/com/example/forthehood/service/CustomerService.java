package com.example.forthehood.service;

import com.example.forthehood.dto.CustomerProfileResponse;
import com.example.forthehood.dto.UpdateCustomerProfileRequest;
import com.example.forthehood.entity.Account;
import com.example.forthehood.entity.Customer;
import com.example.forthehood.repository.AccountRepository;
import com.example.forthehood.repository.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public CustomerService(AccountRepository accountRepository,
                           CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    private Customer getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account not found for current user"));
        return customerRepository.findAll().stream()
                .filter(c -> c.getAccount().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Customer profile not found for current user"));
    }

    public CustomerProfileResponse getCurrentCustomerProfile() {
        Customer customer = getCurrentCustomer();
        return new CustomerProfileResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getAddress()
        );
    }

    @Transactional
    public CustomerProfileResponse updateCurrentCustomerProfile(UpdateCustomerProfileRequest request) {
        Customer customer = getCurrentCustomer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        Customer saved = customerRepository.save(customer);
        return new CustomerProfileResponse(
                saved.getId(),
                saved.getName(),
                saved.getPhone(),
                saved.getAddress()
        );
    }
}
