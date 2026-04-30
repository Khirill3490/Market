package ru.example.userservice.exception;

import ru.example.identitydomain.entity.enums.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Нельзя перевести заказ из статуса " + currentStatus + " в статус " + targetStatus);
    }
}