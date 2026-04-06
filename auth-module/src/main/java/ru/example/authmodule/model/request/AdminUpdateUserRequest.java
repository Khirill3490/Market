package ru.example.authmodule.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.identitydomain.entity.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на обновление пользователя и его компании администратором")
public class AdminUpdateUserRequest {

    @Schema(description = "Email пользователя", example = "user@example.com")
    @Email(message = "Некорректный e-mail")
    private String email;

    @Schema(description = "Название компании", example = "ООО Ромашка")
    @Size(min = 2, max = 120, message = "Длина имени компании 2–120")
    private String companyName;

    @Schema(description = "ИНН компании", example = "7701234567")
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 цифр")
    private String inn;

    @Schema(description = "Город", example = "Москва")
    @Size(min = 2, max = 60, message = "Длина названия города 2–60")
    private String city;

    @Schema(description = "Контактный телефон", example = "+79991234567")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Телефон в формате +79991234567")
    private String phone;

    @Schema(description = "Дополнительный email", example = "manager@example.com")
    @Email(message = "Некорректный дополнительный e-mail")
    private String mail;

    @Schema(description = "Активен ли пользователь", example = "true")
    private Boolean active;

    @Schema(description = "Роль пользователя", example = "ROLE_USER")
    private RoleType role;
}