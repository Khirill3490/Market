package ru.example.productservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ImageCatalog {

    private final List<String> urls;

    public ImageCatalog(
            @Value("${images.catalog:classpath:images/funny_images.txt}") Resource catalog
    ) throws Exception {
        try (var br = new BufferedReader(new InputStreamReader(catalog.getInputStream(), StandardCharsets.UTF_8))) {
            this.urls = br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isBlank() && !s.startsWith("#"))
                    .toList();
        }
        if (urls.isEmpty()) {
            throw new IllegalStateException("Image catalog is empty");
        }
    }

    /** Случайная картинка */
    public String random() {
        int i = ThreadLocalRandom.current().nextInt(urls.size());
        return urls.get(i);
    }

    /** Все ссылки (иммутабельный список) */
    public List<String> all() {
        return urls;
    }
}
