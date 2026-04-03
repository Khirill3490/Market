package ru.example.authmodule.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        description = "Refresh token пользователя",
        example = "c7f6e6d5b4a3495ebf8b0d2c2ab7c9d1",
        requiredMode = Schema.RequiredMode.REQUIRED
)
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
