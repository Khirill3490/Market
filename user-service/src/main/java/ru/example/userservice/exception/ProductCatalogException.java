package ru.example.userservice.exception;

public class ProductCatalogException extends RuntimeException {

    public ProductCatalogException(String message) {
        super(message);
    }

    public ProductCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}