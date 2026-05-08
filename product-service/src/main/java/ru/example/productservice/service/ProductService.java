package ru.example.productservice.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import ru.example.productservice.entity.Product;
import ru.example.productservice.model.request.ProductRequest;

import java.util.List;

public interface ProductService {

    Page<Product> findByName(String name, int page, int size);
    Page<Product> findAll(int page, int size);

    List<Product> getProductsForMainPage();

    Product findById(Long id);
    Product findByPublicId(String publicId);
    Product findByArt(String art);
    List<Product> findByName(String name);
    Product save(Product product, MultipartFile imageFile);
    Product update(Long id, ProductRequest request);
    Product decreaseStock(String publicId, int quantity);
    void deleteByPublicId(String publicId);
    void deleteByArt(String art);
}
