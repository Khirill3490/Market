package ru.example.productservice.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String art) {
        super("Товар с артикулом '" + art + "' уже существует");
    }
}