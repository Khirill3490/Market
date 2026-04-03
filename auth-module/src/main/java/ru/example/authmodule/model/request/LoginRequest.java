package ru.example.authmodule.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на вход в систему")
public class LoginRequest {

    @Schema(
            description = "Email пользователя",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
            description = "Пароль пользователя",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}
