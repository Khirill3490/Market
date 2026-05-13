package ru.example.identitydomain.entity.enums;

public enum OrderStatus {
    CREATED,

    PENDING_STOCK_RESERVATION,
    CONFIRMED,
    STOCK_RESERVATION_FAILED,

    CANCELLATION_REQUESTED,
    CANCELLATION_FAILED,

    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}