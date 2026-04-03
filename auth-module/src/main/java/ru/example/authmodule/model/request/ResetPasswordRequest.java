package ru.example.authmodule.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Токен обязателен")
    private String token;

    @Schema(
            description = "Новый пароль пользователя",
            example = "NewPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Новый пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            message = "Пароль: 8–64 символов, минимум 1 буква и 1 цифра"
    )
    private String newPassword;

    @Schema(
            description = "Подтверждение нового пароля",
            example = "NewPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;

    @Schema(hidden = true)
    @AssertTrue(message = "Пароли должны совпадать")
    public boolean isPasswordsMatch() {
        if (newPassword == null || confirmPassword == null
                || newPassword.isBlank() || confirmPassword.isBlank()) {
            return true;
        }
        return newPassword.equals(confirmPassword);
    }
}