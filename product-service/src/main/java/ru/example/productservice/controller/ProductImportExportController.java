package ru.example.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.example.productservice.service.impl.ProductImportExportService;

import java.io.IOException;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class ProductImportExportController {

    private final ProductImportExportService service;

    @GetMapping("/export")
    public ResponseEntity<Resource> exportProducts() throws IOException {
        System.out.println("В методе");
        return service.exportProductsToJson();
    }

    @GetMapping
    public ResponseEntity<String> hi() {
        System.out.println("В методе");
        return new ResponseEntity<>("hi", HttpStatus.OK);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importProducts(@RequestPart("file") MultipartFile file) throws IOException {
        service.importProductsFromJson(file);
        return ResponseEntity.ok("Products imported successfully");
    }


}
