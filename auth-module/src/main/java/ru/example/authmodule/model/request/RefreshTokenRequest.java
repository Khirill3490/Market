package ru.example.authmodule.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на обновление или инвалидирование refresh token")
public class RefreshTokenRequest {

    @Schema(
            description = "Refresh token пользователя",
            example = "c7f6e6d5b4a3495ebf8b0d2c2ab7c9d1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}
