package ru.example.authmodule.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.identitydomain.entity.enums.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о пользователе")
public class UserUpdatedResponse {

    @Schema(description = "Публичный идентификатор пользователя", example = "a3f9a8c2-1234")
    private String publicId;

    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "ROLE_USER")
    private RoleType role;

    @Schema(description = "Активен ли пользователь", example = "true")
    private boolean active;

    @Schema(description = "Название компании", example = "ООО Ромашка")
    private String companyName;

    @Schema(description = "ИНН компании", example = "7701234567")
    private String inn;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Телефон", example = "+79991234567")
    private String phone;

    @Schema(description = "Дополнительный email", example = "manager@example.com")
    private String mail;
}