package ru.example.authmodule.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Простой текстовый ответ сервера")
public class SimpleResponse {

    @Schema(
            description = "Сообщение о результате операции",
            example = "Операция выполнена успешно"
    )
    private String message;
}
