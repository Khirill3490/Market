package ru.example.authmodule.service;

public interface ActivationService {

    void sendUri(String publicId, String email);

    void activate(String token);
}
