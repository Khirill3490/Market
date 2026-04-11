package ru.example.authmodule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.example.authmodule.model.request.AdminUpdateUserRequest;
import ru.example.authmodule.model.response.UserUpdatedResponse;
import ru.example.authmodule.service.AccountService;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Администрирование пользователей", description = "Эндпоинты для управления пользователями администратором")
public class AdminUserController {

    private final AccountService accountService;

    @Operation(
            summary = "Обновить пользователя",
            description = "Позволяет администратору обновить данные пользователя и его компании"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Email или ИНН уже заняты")
    })
    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserUpdatedResponse> updateUser(
            @PathVariable String publicId,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        return ResponseEntity.ok(accountService.update(publicId, request));
    }
}