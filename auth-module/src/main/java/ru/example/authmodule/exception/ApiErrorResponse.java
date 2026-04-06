package ru.example.authmodule.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
@Schema(description = "Единый формат ответа с ошибкой")
public record ApiErrorResponse(

        @Schema(
                description = "Время возникновения ошибки",
                example = "2026-04-06T14:35:00Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP-статус",
                example = "400"
        )
        int status,

        @Schema(
                description = "Краткое название ошибки",
                example = "Bad Request"
        )
        String error,

        @Schema(
                description = "Основное сообщение об ошибке",
                example = "Некорректные данные запроса"
        )
        String message,

        @Schema(
                description = "Путь запроса",
                example = "/api/v1/auth/register"
        )
        String path,

        @Schema(
                description = "Ошибки валидации по полям"
        )
        Map<String, String> validationErrors
) {
}