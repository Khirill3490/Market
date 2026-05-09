package ru.example.productservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
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
import ru.example.productservice.model.request.DecreaseStockRequest;
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

    @GetMapping("/public/{publicId}")
    public ResponseEntity<ProductResponse> getByPublicId(
            @PathVariable @NotBlank(message = "publicId товара не должен быть пустым") String publicId
    ) {
        ProductResponse response = productMapper.toResponse(productService.findByPublicId(publicId));

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

    @DeleteMapping("/public/{publicId}")
    public ResponseEntity<Void> deleteByPublicId(
            @PathVariable @NotBlank(message = "publicId товара не должен быть пустым") String publicId
    ) {
        productService.deleteByPublicId(publicId);

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
    public ResponseEntity<List<ProductResponse>> generateProducts(
            @PathVariable
            @Positive(message = "count должен быть положительным")
            @Max(value = 1000, message = "count не должен быть больше 1000")
            int count
    ) {
        List<ProductResponse> response = generationService.save(count)
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/public/{publicId}/stock/decrease")
    public ResponseEntity<ProductResponse> decreaseStock(
            @PathVariable @NotBlank(message = "publicId товара не должен быть пустым") String publicId,
            @Valid @RequestBody DecreaseStockRequest request
    ) {
        Product product = productService.decreaseStock(publicId, request.quantity());
        ProductResponse response = productMapper.toResponse(product);

        return ResponseEntity.ok(response);
    }
}