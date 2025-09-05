package ru.example.authmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.authmodule.model.request.ForgotPasswordRequest;
import ru.example.authmodule.model.request.ResetPasswordRequest;
import ru.example.authmodule.redis.service.PasswordResetService;
import ru.example.common.dto.SimpleMessageResponse;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService service;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody ForgotPasswordRequest request) {
        service.requestReset(request.getEmail());
        return ResponseEntity.ok(new SimpleMessageResponse("Ссылка на восстановление пароля была отправлена на Ваш email"));
    }



    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ResetPasswordRequest request) {
        // проверь длину/политику пароля вручную или через Bean Validation
        service.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new SimpleMessageResponse("Пароль успешно изменен"));
    }
}
