package ru.example.userservice.model.event;

public record OrderCancellationRequestedEvent(
        String orderPublicId
) {
}