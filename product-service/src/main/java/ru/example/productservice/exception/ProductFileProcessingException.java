package ru.example.productservice.exception;

public class ProductFileProcessingException extends RuntimeException {

    public ProductFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductFileProcessingException(String message) {
        super(message);
    }
}