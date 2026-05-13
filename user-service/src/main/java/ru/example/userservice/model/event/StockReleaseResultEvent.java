package ru.example.userservice.model.event;

public record StockReleaseResultEvent(
        String orderPublicId,
        boolean success,
        String reason
) {
}