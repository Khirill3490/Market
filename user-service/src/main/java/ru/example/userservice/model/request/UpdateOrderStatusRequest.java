package ru.example.userservice.model.request;

import jakarta.validation.constraints.NotNull;
import ru.example.identitydomain.entity.enums.OrderStatus;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Статус заказа обязателен")
        OrderStatus status
) {
}