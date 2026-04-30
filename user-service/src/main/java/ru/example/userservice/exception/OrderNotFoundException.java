package ru.example.userservice.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String publicId) {
        super("Заказ с publicId=" + publicId + " не найден");
    }
}