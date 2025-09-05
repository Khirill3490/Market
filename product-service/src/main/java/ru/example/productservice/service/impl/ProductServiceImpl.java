package ru.example.productservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.example.common.entity.Product;
import ru.example.common.exception.EntityAlreadyExistsException;
import ru.example.common.exception.EntityNotFoundException;
import ru.example.productservice.model.request.ProductRequest;
import ru.example.productservice.repository.ProductRepository;
import ru.example.productservice.service.ProductService;

import java.util.List;

@Service
@RequiredArgsConstructor
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
        return productRepository.findRandom8();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ошибка. Продукт с id " + id + " не найден"));
    }

    @Override
    public Product findByArt(String art) {
        return productRepository.findByArtContainingIgnoreCase(art)
                .orElseThrow(() -> new EntityNotFoundException("Ошибка. Продукт с art " + art + " не найден"));
    }

    @Override
    public List<Product> findByName(String name) {
        return List.of();
    }

    @Override
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
    public Product update(Long id, ProductRequest request) {
        throwExceptionIfArtExists(request.getArt());
        Product product = findById(id);

        return null;
    }


    @Override
    public void deleteById(Long id) {


    }

    @Override
    public void deleteByArt(String art) {

    }

    private void throwExceptionIfArtExists(String art) {
        if (productRepository.existsByArtContainingIgnoreCase(art)) {
            throw new EntityAlreadyExistsException("Ошибка. Товар с данным арт уже существует");
        }
    }
}
