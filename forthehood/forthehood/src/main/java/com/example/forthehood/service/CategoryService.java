package com.example.forthehood.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.forthehood.dto.CategoryCreateUpdateRequest;
import com.example.forthehood.dto.CategoryResponse;
import com.example.forthehood.entity.Category;
import com.example.forthehood.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getStatus()))
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryCreateUpdateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .status(request.getStatus())
                .build();
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getStatus());
    }

    public CategoryResponse updateCategory(Long id, CategoryCreateUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        category.setName(request.getName());
        category.setStatus(request.getStatus());
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getStatus());
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
