package ru.example.userservice.model.response;

import java.math.BigDecimal;

public record ProductCatalogResponse(
        String publicId,
        String art,
        String brand,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        String inf,
        String ext,
        String img,
        String url,
        String unit,
        String sml,
        String cat,
        String bar
) {
}