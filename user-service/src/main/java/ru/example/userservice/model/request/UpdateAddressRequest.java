package ru.example.userservice.model.request;

import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(

        @Size(max = 100, message = "Название адреса не должно быть длиннее 100 символов")
        String label,

        @Size(max = 100, message = "Страна не должна быть длиннее 100 символов")
        String country,

        @Size(max = 100, message = "Регион не должен быть длиннее 100 символов")
        String region,

        @Size(max = 100, message = "Город не должен быть длиннее 100 символов")
        String city,

        @Size(max = 255, message = "Улица не должна быть длиннее 255 символов")
        String street,

        @Size(max = 32, message = "Номер дома не должен быть длиннее 32 символов")
        String house,

        @Size(max = 32, message = "Квартира не должна быть длиннее 32 символов")
        String apartment,

        @Size(max = 32, message = "Почтовый индекс не должен быть длиннее 32 символов")
        String postalCode,

        @Size(max = 255, message = "Комментарий не должен быть длиннее 255 символов")
        String comment,

        Boolean isDefault
) {
}