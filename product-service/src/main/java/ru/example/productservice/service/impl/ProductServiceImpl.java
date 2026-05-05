package ru.example.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.example.productservice.entity.Product;
import ru.example.productservice.exception.ProductAlreadyExistsException;
import ru.example.productservice.exception.ProductNotFoundException;
import ru.example.productservice.model.request.ProductRequest;
import ru.example.productservice.repository.ProductRepository;
import ru.example.productservice.service.ProductService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;

    @Override
    public Page<Product> findByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> findAll(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public List<Product> getProductsForMainPage() {
        return productRepository.findAllWithBrandAndCategory()
                .stream()
                .limit(8)
                .toList();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product findByPublicId(String publicId) {
        return productRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Товар с publicId=" + publicId + " не найден"
                ));
    }

    @Override
    public Product findByArt(String art) {
        return productRepository.findByArtIgnoreCase(art)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Товар с артикулом '" + art + "' не найден"
                ));
    }

    @Override
    public List<Product> findByName(String name) {
        return productRepository
                .findByNameContainingIgnoreCase(name, Pageable.unpaged())
                .getContent();
    }

    @Override
    @Transactional
    public Product save(Product product, MultipartFile imageFile) {
        throwExceptionIfArtExists(product.getArt());

        String imageUrl = imageStorageService.save(
                imageFile,
                product.getBrand().getName(),
                product.getArt()
        );

        product.setImg(imageUrl);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, ProductRequest request) {
        throw new UnsupportedOperationException("Обновление товара пока не реализовано");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void deleteByArt(String art) {
        Product product = findByArt(art);
        productRepository.delete(product);
    }

    private void throwExceptionIfArtExists(String art) {
        if (productRepository.existsByArtIgnoreCase(art)) {
            throw new ProductAlreadyExistsException(art);
        }
    }
}