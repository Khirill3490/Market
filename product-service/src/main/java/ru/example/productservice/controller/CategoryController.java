package ru.example.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.productservice.mapper.CategoryMapper;
import ru.example.productservice.model.response.CategoryResponse;
import ru.example.productservice.model.response.CategorySimpleResponse;
import ru.example.productservice.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService
                .findAll()
                .stream()
                .map(categoryMapper::fromCategoryToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/random")
    public ResponseEntity<List<CategorySimpleResponse>> getCategoriesForMainPage() {
        return ResponseEntity.ok(categoryService
                .findCategoryForMain()
                .stream()
                .map(categoryMapper::toSimpleResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/with/count")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesWithProductCount() {
        return ResponseEntity.ok(categoryService
                .findAllAndGetProductsCount()
                .stream()
                .map(categoryMapper::fromCategoryToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryMapper.fromCategoryToResponse(categoryService.findById(id)));
    }

    @GetMapping("/with/count/{id}")
    public ResponseEntity<CategoryResponse> getByIdWithProductCount(@PathVariable Long id) {
        return ResponseEntity.ok(categoryMapper.fromCategoryToResponse(categoryService.findByIdAndGetProductsCount(id)));
    }
}
