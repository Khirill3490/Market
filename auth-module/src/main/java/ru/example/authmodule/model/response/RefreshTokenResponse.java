package ru.example.authmodule.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с новой парой токенов после обновления сессии")
public class RefreshTokenResponse {

    @Schema(
            description = "JWT access token",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Refresh token",
            example = "d1a2b3c4e5f64789aabbccddeeff0011"
    )
    private String refreshToken;

}
