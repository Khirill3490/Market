package ru.example.userservice.model.request;

import jakarta.validation.constraints.Size;

public record UpdateCurrentUserRequest(

        @Size(max = 100, message = "Имя не должно быть длиннее 100 символов")
        String firstName,

        @Size(max = 100, message = "Фамилия не должна быть длиннее 100 символов")
        String lastName,

        @Size(max = 32, message = "Телефон не должен быть длиннее 32 символов")
        String phone
) {
}