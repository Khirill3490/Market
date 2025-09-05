package ru.example.productservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.example.common.entity.Product;
import ru.example.productservice.mapper.ProductMapper;
import ru.example.productservice.model.response.ProductResponse;
import ru.example.productservice.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImportExportService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;


    public ResponseEntity<Resource> exportProductsToJson() throws IOException {
        List<ProductResponse> products = productRepository
                .findAll()
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path exportPath = Paths.get("exports/products_" + timestamp + ".json");

        // Создать директорию, если нет
        Files.createDirectories(exportPath.getParent());

        // Записать в файл
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(exportPath.toFile(), products);

        Resource resource = new FileSystemResource(exportPath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportPath.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    public void importProductsFromJson(MultipartFile file) throws IOException {
        List<Product> products = objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<List<Product>>() {}
        );
        productRepository.saveAll(products);
    }


}
