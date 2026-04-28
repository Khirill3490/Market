package ru.example.productservice.exception;

public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(String brandName) {
        super("Бренд с названием '" + brandName + "' не найден");
    }
}