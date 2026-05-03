package ru.example.userservice.model.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        String publicId,
        List<CartItemResponse> items,
        Integer totalItems,
        BigDecimal totalAmount
) {
}