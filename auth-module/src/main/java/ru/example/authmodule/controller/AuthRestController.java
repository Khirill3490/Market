package ru.example.authmodule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.example.authmodule.model.response.CurrentAccountResponse;
import ru.example.authmodule.security.service.CurrentAccountService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Current account",
        description = "Получение текущего account context по валидному Keycloak JWT"
)
public class AuthRestController {

    private final CurrentAccountService currentAccountService;

    @Operation(
            summary = "Получить текущий аккаунт",
            description = "Возвращает данные из Keycloak JWT и информацию о локальном Account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация успешно получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @GetMapping("/me")
    public ResponseEntity<CurrentAccountResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(currentAccountService.getCurrentAccount(jwt));
    }
}