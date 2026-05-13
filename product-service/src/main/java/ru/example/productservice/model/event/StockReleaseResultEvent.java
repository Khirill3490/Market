package ru.example.productservice.model.event;

public record StockReleaseResultEvent(
        String orderPublicId,
        boolean success,
        String reason
) {
}