package com.example.forthehood.service;

import com.example.forthehood.dto.CreateEmployeeRequest;
import com.example.forthehood.dto.EmployeeResponse;
import com.example.forthehood.entity.Account;
import com.example.forthehood.entity.Employee;
import com.example.forthehood.entity.Role;
import com.example.forthehood.enums.AccountStatus;
import com.example.forthehood.enums.RoleName;
import com.example.forthehood.repository.AccountRepository;
import com.example.forthehood.repository.EmployeeRepository;
import com.example.forthehood.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository,
                          EmployeeRepository employeeRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EmployeeResponse adminCreateEmployee(CreateEmployeeRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE role not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        Account adminAccount = accountRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Admin account not found"));

        Employee adminEmployee = employeeRepository.findAll().stream()
                .filter(e -> e.getAccount().getId().equals(adminAccount.getId()))
                .findFirst()
                .orElse(null);

        Account employeeAccount = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(AccountStatus.ACTIVE)
                .role(employeeRole)
                .createdAt(LocalDateTime.now())
                .build();
        employeeAccount = accountRepository.save(employeeAccount);

        Employee employee = Employee.builder()
                .account(employeeAccount)
                .name(request.getName())
                .position(request.getPosition())
                .createdBy(adminEmployee)
                .build();
        employee = employeeRepository.save(employee);

        return new EmployeeResponse(employee.getId(), employeeAccount.getEmail(), employee.getName(), employee.getPosition());
    }

    public void lockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));
        account.setStatus(AccountStatus.LOCKED);
        accountRepository.save(account);
    }
}
