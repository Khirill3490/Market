package ru.example.productservice.model.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    private String art;           // ART – Артикул товара
    private String brand;           // MAN – Производитель
    private String name;
    private String inf;           // INF – Краткая информация
    private String ext;           // EXT – Расширенная информация
    private String url;           // URL – Ссылка на страницу товара
    private String unit;          // UNIT – Единица измерения (шт., кг и т.д.)
    private String sml;           // SML – Размер, вес или объём
    private String cat;           // CAT – Категория
    private String bar;           // BAR – Штрихкод
}
