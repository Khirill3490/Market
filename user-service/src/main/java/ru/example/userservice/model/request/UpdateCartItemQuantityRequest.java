package ru.example.userservice.model.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(

        @Min(value = 1, message = "Количество должно быть не меньше 1")
        Integer quantity
) {
}