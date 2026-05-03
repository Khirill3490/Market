package ru.example.userservice.model.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productPublicId,
        String productName,
        String productImage,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}