package ru.example.authmodule.exception;

public class LegacyAuthFlowDisabledException extends RuntimeException {
    public LegacyAuthFlowDisabledException(String message) {
        super(message);
    }
}