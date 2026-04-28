package ru.example.productservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.example.productservice.entity.Product;
import ru.example.productservice.mapper.ProductMapper;
import ru.example.productservice.model.request.ProductPagSearchRequest;
import ru.example.productservice.model.request.ProductRequest;
import ru.example.productservice.model.response.ProductResponse;
import ru.example.productservice.service.ProductService;
import ru.example.productservice.util.DataGenerationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final DataGenerationService generationService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findWithPagination(
            @Valid @ModelAttribute ProductPagSearchRequest request
    ) {
        Page<ProductResponse> response = productService
                .findAll(request.getPage(), request.getSize())
                .map(productMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductResponse>> getProductsForMainPage() {
        List<ProductResponse> response = productService
                .getProductsForMainPage()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    public ResponseEntity<Page<ProductResponse>> findByNameWithPagination(
            @RequestParam("name") String name,
            @Valid @ModelAttribute ProductPagSearchRequest request
    ) {
        Page<ProductResponse> response = productService
                .findByName(name, request.getPage(), request.getSize())
                .map(productMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable @Positive(message = "id товара должен быть положительным") Long id
    ) {
        ProductResponse response = productMapper.toResponse(productService.findById(id));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/art/{art}")
    public ResponseEntity<ProductResponse> findByArt(
            @PathVariable String art
    ) {
        ProductResponse response = productMapper.toResponse(productService.findByArt(art));

        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestPart("data") ProductRequest request,
            @RequestPart("image") MultipartFile imageFile
    ) {
        Product product = productMapper.toProduct(request);
        Product savedProduct = productService.save(product, imageFile);
        ProductResponse response = productMapper.toResponse(savedProduct);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @Positive(message = "id товара должен быть положительным") Long id
    ) {
        productService.deleteById(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/art/{art}")
    public ResponseEntity<Void> deleteByArt(
            @PathVariable String art
    ) {
        productService.deleteByArt(art);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/gen/{count}")
    public ResponseEntity<String> generateProducts(
            @PathVariable @Positive(message = "count должен быть положительным") int count
    ) {
        generationService.save(count);

        return ResponseEntity.ok("Сгенерировано товаров: " + count);
    }
}