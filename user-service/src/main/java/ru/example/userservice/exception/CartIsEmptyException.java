package ru.example.userservice.exception;

public class CartIsEmptyException extends RuntimeException {

    public CartIsEmptyException() {
        super("Нельзя создать заказ из пустой корзины");
    }
}