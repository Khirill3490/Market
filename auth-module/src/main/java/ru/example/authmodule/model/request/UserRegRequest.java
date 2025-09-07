package ru.example.authmodule.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegRequest {

    @NotBlank(message = "E-mail обязателен")
    @Email(message = "Некорректный e-mail")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            message = "Пароль: 8–64 символов, минимум 1 буква и 1 цифра"
    )
    private String password;

    @NotBlank(message = "Повтор пароля обязателен")
    private String confirmPassword;

    @NotBlank(message = "Название компании обязательно")
    @Size(min = 2, max = 120, message = "Длина имени компании 2–120")
    private String companyName;

    @NotBlank(message = "ИНН обязателен")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 цифр")
    private String inn;

    @NotBlank(message = "Город обязателен")
    @Size(min = 2, max = 60, message = "Длина названия города 2–60")
    private String city;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Телефон в формате +79991234567")
    private String phone;

    @Email(message = "Некорректный дополнительный e-mail")
    private String mail; // опционально

    // Проверяем только совпадение паролей (сложность уже проверяет @Pattern на поле password)
    @AssertTrue(message = "Пароли должны совпадать")
    public boolean isPasswordsMatch() {
        // если пусто — пусть сработают @NotBlank на полях, тут не дублируем ошибку
        if (password == null || confirmPassword == null || password.isBlank() || confirmPassword.isBlank()) {
            return true;
        }
        return password.equals(confirmPassword);
    }
}