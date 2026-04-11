package ru.example.authmodule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.example.authmodule.model.request.ForgotPasswordRequest;
import ru.example.authmodule.model.request.ResetPasswordRequest;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.authmodule.model.response.CurrentAccountResponse;
import ru.example.authmodule.model.response.SimpleResponse;
import ru.example.authmodule.redis.service.PasswordResetService;
import ru.example.authmodule.security.service.CurrentAccountService;
import ru.example.authmodule.security.service.SecurityService;
import ru.example.authmodule.service.ActivationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Управление аккаунтом",
        description = "Эндпоинты для регистрации, активации аккаунта, восстановления пароля и работы с текущим пользователем"
)
public class AuthRestController {

    private final CurrentAccountService currentAccountService;


    @Operation(
            summary = "Текущий пользователь",
            description = "Возвращает данные текущего пользователя из Keycloak JWT и локального профиля, если он найден"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные текущего пользователя получены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @GetMapping("/me")
    public ResponseEntity<CurrentAccountResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(currentAccountService.getCurrentUser(jwt));
    }

}