package ru.example.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.example.productservice.service.impl.ProductImportExportService;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class ProductImportExportController {

    private final ProductImportExportService productImportExportService;

    @GetMapping("/export")
    public ResponseEntity<Resource> exportProducts() {
        Resource resource = productImportExportService.exportProductsToJson();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\""
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importProducts(
            @RequestPart("file") MultipartFile file
    ) {
        int importedCount = productImportExportService.importProductsFromJson(file);

        return ResponseEntity.ok("Импортировано товаров: " + importedCount);
    }
}