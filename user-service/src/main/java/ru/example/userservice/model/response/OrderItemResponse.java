package ru.example.userservice.model.response;

public record OrderItemResponse(
        String productPublicId,
        String productName,
        String productImage,
        Integer quantity
) {
}