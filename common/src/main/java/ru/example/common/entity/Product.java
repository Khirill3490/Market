package ru.example.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "products", indexes = @Index(name = "idx_product_art", columnList = "art"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String art;           // ART – Артикул товара

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "man", nullable = false)
    private Brand brand;          // MAN – Производитель
    private String name;

    private String inf;           // INF – Краткая информация
    private String ext;           // EXT – Расширенная информация
    private String img;           // IMG – Путь к изображению или имя файла
    private String url;           // URL – Ссылка на страницу товара
    private String unit;          // UNIT – Единица измерения (шт., кг и т.д.)
    private String sml;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)// SML – Размер, вес или объём
    private Category category;    // CAT – Категория
    private String bar;           // BAR – Штрихкод
}
