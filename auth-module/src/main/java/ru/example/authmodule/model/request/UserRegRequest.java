package ru.example.authmodule.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Запрос на регистрацию пользователя и компании")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegRequest {

    @Schema(
            description = "Email пользователя для входа в систему",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "E-mail обязателен")
    @Email(message = "Некорректный e-mail")
    private String email;

    @Schema(
            description = "Пароль пользователя. Должен содержать от 3 до 64 символов, минимум одну букву и одну цифру",
            example = "Password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{3,64}$",
            message = "Пароль: 3–64 символов, минимум 1 буква и 1 цифра"
    )
    private String password;

    @Schema(
            description = "Повтор пароля для подтверждения",
            example = "Password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Повтор пароля обязателен")
    private String confirmPassword;

    @Schema(
            description = "Название компании",
            example = "ООО Ромашка",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Название компании обязательно")
    @Size(min = 2, max = 120, message = "Длина имени компании 2–120")
    private String companyName;

    @Schema(
            description = "ИНН компании. Допустимо 10 или 12 цифр",
            example = "7701234567",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "ИНН обязателен")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 цифр")
    private String inn;

    @Schema(
            description = "Город регистрации или нахождения компании",
            example = "Москва",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Город обязателен")
    @Size(min = 2, max = 60, message = "Длина названия города 2–60")
    private String city;

    @Schema(
            description = "Контактный телефон",
            example = "+79991234567",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Телефон в формате +79991234567")
    private String phone;

    @Schema(
            description = "Дополнительный email для связи. Поле необязательное",
            example = "manager@example.com"
    )
    @Email(message = "Некорректный дополнительный e-mail")
    private String mail;

    @Schema(
            description = "Проверка совпадения пароля и подтверждения пароля",
            hidden = true
    )
    @AssertTrue(message = "Пароли должны совпадать")
    public boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null || password.isBlank() || confirmPassword.isBlank()) {
            return true;
        }
        return password.equals(confirmPassword);
    }
}