package ru.example.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ru.example.productservice.entity.Product;
import ru.example.productservice.mapper.ProductMapper;
import ru.example.productservice.model.request.ProductRequest;
import ru.example.productservice.model.request.ProductPagSearchRequest;
import ru.example.productservice.model.response.ProductResponse;
import ru.example.productservice.service.ProductService;
import ru.example.productservice.util.DataGenerationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final DataGenerationService generationService;


    @GetMapping
    public Page<ProductResponse> findWithPagination(ProductPagSearchRequest request) {
        return productService
                .findAll(request.getPage(), request.getSize())
                .map(productMapper::toResponse);
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductResponse>> getProductsForMainPage() {
        return ResponseEntity.ok(productService
                .getProductsForMainPage()
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList()));
    }



    @GetMapping("/search/name")
    public Page<ProductResponse> findByNameWithPagination(
            @RequestParam("name") String name,
            @ModelAttribute ProductPagSearchRequest request
    ) {
        return productService
                .findByName(name, request.getPage(), request.getSize())
                .map(productMapper::toResponse);
    }

    @GetMapping("/search/id/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return productMapper.toResponse(productService.findById(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productMapper.toResponse(productService.findById(id)));
    }

    @GetMapping("/search/art/{art}")
    public ProductResponse findByArt(@PathVariable String art) {
        return productMapper.toResponse(productService.findByArt(art));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> create(@RequestPart("data") ProductRequest jsonRequest,
                                                  @RequestPart("image") MultipartFile imageFile) {

        Product product = productMapper.toProduct(jsonRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toResponse(productService.save(product, imageFile)));
    }



//    @PutMapping("/{id}")
//    public ResponseEntity<ProductResponse> update(
//            @PathVariable Long id,
//            @RequestBody @Valid ProductRequest request) {
//        return ResponseEntity.ok(productService.update(id, request));
//    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/art/{art}")
    public ResponseEntity<Void> deleteById(@PathVariable String art) {
        productService.deleteByArt(art);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/gen/{count}")
    public String gen(@PathVariable int count) {
        generationService.save(count);
        return "Сгенерировано";
    }
}
