package ru.example.productservice.model.event;

public record StockReservationResultEvent(
        String orderPublicId,
        boolean success,
        String reason
) {
}