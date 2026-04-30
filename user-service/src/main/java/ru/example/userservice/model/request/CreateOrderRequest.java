package ru.example.userservice.model.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(

        @NotBlank(message = "Адрес доставки обязателен")
        String addressPublicId
) {
}