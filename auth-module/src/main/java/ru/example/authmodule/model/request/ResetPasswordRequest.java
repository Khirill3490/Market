package ru.example.authmodule.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Запрос на установку нового пароля")
public class ResetPasswordRequest {

    @Schema(
            description = "Токен для сброса пароля, полученный по email",
            example = "c7f6e6d5b4a3495ebf8b0d2c2ab7c9d1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;

    @Schema(
            description = "Новый пароль пользователя",
            example = "newPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;

    @Schema(
            description = "Подтверждение нового пароля",
            example = "NewStrongPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String confirmPassword;
}
