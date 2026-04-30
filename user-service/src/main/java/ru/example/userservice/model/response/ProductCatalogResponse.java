package ru.example.userservice.model.response;

public record ProductCatalogResponse(
        Long id,
        String art,
        String brand,
        String name,
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