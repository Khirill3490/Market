package ru.example.productservice.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Артикул товара обязателен")
    @Size(max = 255, message = "Артикул товара не должен быть длиннее 255 символов")
    private String art;

    @NotBlank(message = "Бренд обязателен")
    @Size(max = 255, message = "Название бренда не должно быть длиннее 255 символов")
    private String brand;

    @NotBlank(message = "Название товара обязательно")
    @Size(max = 255, message = "Название товара не должно быть длиннее 255 символов")
    private String name;

    @NotNull(message = "Цена товара обязательна")
    @DecimalMin(value = "0.00", inclusive = true, message = "Цена товара не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "Количество товара на складе обязательно")
    @Min(value = 0, message = "Количество товара на складе не может быть меньше 0")
    private Integer stockQuantity;

    @Size(max = 2000, message = "Краткое описание слишком длинное")
    private String inf;

    @Size(max = 5000, message = "Расширенное описание слишком длинное")
    private String ext;

    @Size(max = 1024, message = "URL товара не должен быть длиннее 1024 символов")
    private String url;

    @Size(max = 64, message = "Единица измерения не должна быть длиннее 64 символов")
    private String unit;

    @Size(max = 255, message = "Размер/вес/объём не должен быть длиннее 255 символов")
    private String sml;

    @NotBlank(message = "Категория обязательна")
    @Size(max = 255, message = "Название категории не должно быть длиннее 255 символов")
    private String cat;

    @Size(max = 255, message = "Штрихкод не должен быть длиннее 255 символов")
    private String bar;
}