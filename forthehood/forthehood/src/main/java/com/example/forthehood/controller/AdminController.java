package com.example.forthehood.controller;

import com.example.forthehood.dto.*;
import com.example.forthehood.service.AccountService;
import com.example.forthehood.service.CategoryService;
import com.example.forthehood.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final ProductService productService;

    public AdminController(AccountService accountService,
                          CategoryService categoryService,
                          ProductService productService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @PostMapping("/create-employee")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(accountService.adminCreateEmployee(request));
    }

    @PostMapping("/accounts/{id}/lock")
    public ResponseEntity<Void> lockAccount(@PathVariable("id") Long id) {
        accountService.lockAccount(id);
        return ResponseEntity.ok().build();
    }

    // CATEGORY CRUD

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateUpdateRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") Long id,
                                                           @Valid @RequestBody CategoryCreateUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    // PRODUCT CRUD

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateUpdateRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long id,
                                                         @Valid @RequestBody ProductCreateUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
