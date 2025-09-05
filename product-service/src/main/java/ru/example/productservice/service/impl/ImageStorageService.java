package ru.example.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final Path uploadPath = Paths.get("uploads");

    public String save(MultipartFile file, String brandName, String art) {
        try {
            String sanitizedBrand = brandName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String originalFilename = file.getOriginalFilename();

            Path imagePath = uploadPath.resolve("images")
                    .resolve(sanitizedBrand)
                    .resolve(art);

            if (!Files.exists(imagePath)) {
                Files.createDirectories(imagePath);
            }

            if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
                return ""; // или другое значение по умолчанию
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String filename = timestamp + "_" + fileExtension;
            Path filePath = imagePath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Пример возвращаемого URL
            return "/files/images/" + sanitizedBrand + "/" + art + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }
    }



}
