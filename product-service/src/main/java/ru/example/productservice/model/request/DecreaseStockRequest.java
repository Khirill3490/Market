package ru.example.productservice.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DecreaseStockRequest(
        @NotNull(message = "quantity не должен быть null")
        @Min(value = 1, message = "quantity должен быть больше 0")
        Integer quantity
) {
}