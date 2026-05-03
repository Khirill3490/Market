package ru.example.userservice.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddCartItemRequest(

        @NotBlank(message = "Идентификатор товара обязателен")
        @Size(max = 64, message = "Идентификатор товара не должен быть длиннее 64 символов")
        String productPublicId,

        @NotNull(message = "Количество обязательно")
        @Min(value = 1, message = "Количество должно быть не меньше 1")
        Integer quantity
) {
}