package ru.example.productservice.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long categoryId) {
        super("Категория с id=" + categoryId + " не найдена");
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}