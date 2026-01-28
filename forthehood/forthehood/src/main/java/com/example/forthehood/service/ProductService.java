package com.example.forthehood.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.forthehood.dto.ProductCreateUpdateRequest;
import com.example.forthehood.dto.ProductResponse;
import com.example.forthehood.entity.Category;
import com.example.forthehood.entity.Product;
import com.example.forthehood.repository.CategoryRepository;
import com.example.forthehood.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> getProductList() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getStock(),
                        p.getStatus(),
                        p.getCategory() != null ? p.getCategory().getId() : null,
                        p.getCategory() != null ? p.getCategory().getName() : null
                ))
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(ProductCreateUpdateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .status(request.getStatus())
                .category(category)
                .build();
        Product saved = productRepository.save(product);
        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                saved.getStock(),
                saved.getStatus(),
                saved.getCategory() != null ? saved.getCategory().getId() : null,
                saved.getCategory() != null ? saved.getCategory().getName() : null
        );
    }

    public ProductResponse updateProduct(Long id, ProductCreateUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                saved.getStock(),
                saved.getStatus(),
                saved.getCategory() != null ? saved.getCategory().getId() : null,
                saved.getCategory() != null ? saved.getCategory().getName() : null
        );
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
