package ru.example.userservice.model.response;

import ru.example.identitydomain.entity.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        String publicId,
        OrderStatus status,
        String deliveryAddressPublicId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<OrderItemResponse> items,
        Integer totalItems
) {
}