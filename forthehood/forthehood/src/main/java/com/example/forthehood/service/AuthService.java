package com.example.forthehood.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.forthehood.dto.ChangePasswordRequest;
import com.example.forthehood.dto.LoginRequest;
import com.example.forthehood.dto.LoginResponse;
import com.example.forthehood.dto.RegisterCustomerRequest;
import com.example.forthehood.dto.RegisterCustomerResponse;
import com.example.forthehood.entity.Account;
import com.example.forthehood.entity.Customer;
import com.example.forthehood.entity.Role;
import com.example.forthehood.enums.AccountStatus;
import com.example.forthehood.enums.RoleName;
import com.example.forthehood.repository.AccountRepository;
import com.example.forthehood.repository.CustomerRepository;
import com.example.forthehood.repository.RoleRepository;
import com.example.forthehood.security.CustomUserDetails;
import com.example.forthehood.security.JwtUtil;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RoleRepository roleRepository,
                       AccountRepository accountRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        Account account = userDetails.getAccount();
        String roleName = account.getRole().getName().name();

        return new LoginResponse(token, roleName);
    }

    public RegisterCustomerResponse registerCustomer(RegisterCustomerRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not found"));

        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(AccountStatus.ACTIVE)
                .role(customerRole)
                .createdAt(LocalDateTime.now())
                .build();
        Account savedAccount = accountRepository.save(account);

        Customer customer = Customer.builder()
                .account(savedAccount)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        return new RegisterCustomerResponse(savedCustomer.getId(), savedAccount.getEmail());
    }

    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account not found for current user"));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }
}
