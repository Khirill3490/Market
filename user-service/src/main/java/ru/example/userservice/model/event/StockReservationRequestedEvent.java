package ru.example.userservice.model.event;

import java.util.List;

public record StockReservationRequestedEvent(
        String orderPublicId,
        List<Item> items
) {
    public record Item(
            String productPublicId,
            Integer quantity
    ) {
    }
}