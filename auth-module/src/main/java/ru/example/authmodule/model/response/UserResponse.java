package ru.example.authmodule.model.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.identitydomain.entity.enums.RoleType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Информация о пользователе")
public class UserResponse {

    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @Schema(description = "Список ролей пользователя")
    private List<RoleType> roles;
}
