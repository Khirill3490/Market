package ru.example.userservice.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestParams {

    @Min(value = 0, message = "Номер страницы не может быть меньше 0")
    private int page = 0;

    @Min(value = 1, message = "Размер страницы должен быть не меньше 1")
    @Max(value = 100, message = "Размер страницы не должен быть больше 100")
    private int size = 20;
}