package ru.example.productservice.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Товар с id=" + productId + " не найден");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}