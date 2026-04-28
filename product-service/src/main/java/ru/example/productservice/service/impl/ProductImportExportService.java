package ru.example.productservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.example.productservice.entity.Product;
import ru.example.productservice.exception.ProductAlreadyExistsException;
import ru.example.productservice.exception.ProductFileProcessingException;
import ru.example.productservice.mapper.ProductMapper;
import ru.example.productservice.model.request.ProductRequest;
import ru.example.productservice.model.response.ProductResponse;
import ru.example.productservice.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImportExportService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;

    @Value("${spring.file.export:exports/products.json}")
    private String exportFilePath;

    public Resource exportProductsToJson() {
        try {
            List<ProductResponse> products = productRepository
                    .findAll()
                    .stream()
                    .map(productMapper::toResponse)
                    .toList();

            Path exportPath = buildExportPath();

            Files.createDirectories(exportPath.getParent());

            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(exportPath.toFile(), products);

            return new FileSystemResource(exportPath);
        } catch (IOException exception) {
            throw new ProductFileProcessingException(
                    "Не удалось экспортировать товары в JSON",
                    exception
            );
        }
    }

    @Transactional
    public int importProductsFromJson(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProductFileProcessingException("Файл для импорта не передан или пустой");
        }

        try {
            List<ProductRequest> requests = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<ProductRequest>>() {
                    }
            );

            List<Product> products = requests
                    .stream()
                    .map(this::mapToProductForImport)
                    .toList();

            productRepository.saveAll(products);

            return products.size();
        } catch (IOException exception) {
            throw new ProductFileProcessingException(
                    "Не удалось импортировать товары из JSON",
                    exception
            );
        }
    }

    private Product mapToProductForImport(ProductRequest request) {
        throwExceptionIfArtExists(request.getArt());

        return productMapper.toProduct(request);
    }

    private void throwExceptionIfArtExists(String art) {
        if (productRepository.existsByArtIgnoreCase(art)) {
            throw new ProductAlreadyExistsException(art);
        }
    }

    private Path buildExportPath() {
        Path basePath = Path.of(exportFilePath);

        String timestamp = LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String fileName = "products_" + timestamp + ".json";

        Path parent = basePath.getParent();

        if (parent == null) {
            return Path.of(fileName);
        }

        return parent.resolve(fileName);
    }
}