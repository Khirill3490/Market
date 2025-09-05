package ru.example.authmodule.exception;

public class ErrorMessageGlobal extends RuntimeException {
    public ErrorMessageGlobal(String message) {
        super(message);
    }
}
