package ru.example.productservice.model.event;

public record OrderCancellationRequestedEvent(
        String orderPublicId
) {
}