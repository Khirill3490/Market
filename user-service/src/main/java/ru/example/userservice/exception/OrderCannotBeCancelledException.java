package ru.example.userservice.exception;

import ru.example.identitydomain.entity.enums.OrderStatus;

public class OrderCannotBeCancelledException extends RuntimeException {

    public OrderCannotBeCancelledException(String orderPublicId, OrderStatus status) {
        super("Заказ с publicId=" + orderPublicId + " нельзя отменить в статусе " + status);
    }
}