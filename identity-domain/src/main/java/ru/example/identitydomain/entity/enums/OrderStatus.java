package ru.example.identitydomain.entity.enums;

public enum OrderStatus {
    CREATED,

    PENDING_STOCK_RESERVATION,
    CONFIRMED,
    STOCK_RESERVATION_FAILED,

    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}