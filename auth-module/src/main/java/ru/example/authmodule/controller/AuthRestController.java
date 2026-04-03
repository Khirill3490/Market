package ru.example.authmodule.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.authmodule.model.request.LoginRequest;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.authmodule.model.request.RefreshTokenRequest;
import ru.example.authmodule.model.response.SimpleResponse;
import ru.example.authmodule.model.response.AuthResponse;
import ru.example.authmodule.model.response.RefreshTokenResponse;
import ru.example.authmodule.security.service.SecurityService;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Эндпоинты для аутентификации и регистрации пользователей"
)
public class AuthRestController {

    private final SecurityService securityService;
    private final ActivationService activationService;
    private final UserService userService;

    @Operation(
            summary = "Вход в систему",
            description = "Проверяет email и пароль пользователя и возвращает access token и refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(securityService.authenticateUser(loginRequest));
    }


    @Operation(
            summary = "Регистрация пользователя",
            description = "Создает нового пользователя и запускает процесс активации аккаунта"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Пользователь с такими данными уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<SimpleResponse> register(@RequestBody UserRegRequest request) {
        securityService.register(request);

        return ResponseEntity.ok(
                new SimpleResponse("На ваш email было отправлено сообщение с ссылкой для подтверждения почты"));
    }

    @Operation(
            summary = "Активация аккаунта",
            description = "Активирует аккаунт пользователя по токену активации"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Аккаунт успешно активирован"),
            @ApiResponse(responseCode = "400", description = "Токен активации невалиден или истек")
    })
    @GetMapping("/activate")
    public ResponseEntity<SimpleResponse> register(@RequestParam String token) {
        activationService.activate(token);

        return ResponseEntity.ok(new SimpleResponse("Ваш аккаунт успешно активирован"));
    }

    @Operation(
            summary = "Обновление токенов",
            description = "Проверяет refresh token, ротирует его и возвращает новую пару: access token и refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены"),
            @ApiResponse(responseCode = "401", description = "Refresh token невалиден, истек или уже был использован")
    })
    @PostMapping("/refreshtoken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(securityService.refreshToken(request));
    }

    @Operation(
            summary = "Выход из аккаунта",
            description = "Инвалидирует refresh token в Redis и завершает текущую сессию пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Выход выполнен успешно"),
            @ApiResponse(responseCode = "401", description = "Refresh token невалиден или отсутствует")
    })
    @PostMapping("/logout")
    public ResponseEntity<SimpleResponse> logout(@RequestBody RefreshTokenRequest request) {
        securityService.logout(request);
        return ResponseEntity.ok(new SimpleResponse("Logged out successfully"));
    }

    @Operation(
            summary = "Запрос на сброс пароля",
            description = "Принимает email пользователя и запускает процесс сброса пароля"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Инструкция по сбросу пароля отправлена"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<SimpleResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok().body(new SimpleResponse());
    }

    @Operation(
            summary = "Сброс пароля",
            description = "Устанавливает новый пароль по токену сброса пароля"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Токен сброса невалиден или истек")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<SimpleResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    ...
    }
}
