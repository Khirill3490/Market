package ru.example.productservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.productservice.entity.Brand;
import ru.example.productservice.entity.Product;
import ru.example.productservice.exception.BrandNotFoundException;
import ru.example.productservice.model.request.ProductRequest;
import ru.example.productservice.model.response.ProductResponse;
import ru.example.productservice.repository.BrandRepository;
import ru.example.productservice.service.CategoryService;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final BrandRepository brandRepository;
    private final CategoryService categoryService;

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .art(request.getArt())
                .brand(findBrandByName(request.getBrand()))
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .inf(request.getInf())
                .ext(request.getExt())
                .url(request.getUrl())
                .unit(request.getUnit())
                .sml(request.getSml())
                .category(categoryService.findByName(request.getCat()))
                .bar(request.getBar())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .art(product.getArt())
                .brand(product.getBrand().getName())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .inf(product.getInf())
                .ext(product.getExt())
                .img(product.getImg())
                .url(product.getUrl())
                .unit(product.getUnit())
                .sml(product.getSml())
                .cat(product.getCategory().getName())
                .bar(product.getBar())
                .build();
    }

    private Brand findBrandByName(String brandName) {
        return brandRepository.findByNameEqualsIgnoreCase(brandName)
                .orElseThrow(() -> new BrandNotFoundException(brandName));
    }
}