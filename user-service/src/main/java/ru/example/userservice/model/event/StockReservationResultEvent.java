package ru.example.userservice.model.event;

public record StockReservationResultEvent(
        String orderPublicId,
        boolean success,
        String reason
) {
}