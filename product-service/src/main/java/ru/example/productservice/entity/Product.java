package ru.example.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_art", columnList = "art"),
                @Index(name = "idx_products_category_id", columnList = "category_id"),
                @Index(name = "idx_products_man", columnList = "man")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 64)
    private String publicId;

    /**
     * ART — артикул товара.
     */
    @Column(name = "art")
    private String art;

    /**
     * MAN — производитель / бренд.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "man", nullable = false)
    private Brand brand;

    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /**
     * INF — краткая информация.
     */
    @Column(columnDefinition = "TEXT")
    private String inf;

    /**
     * EXT — расширенная информация.
     */
    @Column(columnDefinition = "TEXT")
    private String ext;

    /**
     * IMG — путь к изображению или имя файла.
     */
    @Column(length = 1024)
    private String img;

    /**
     * URL — ссылка на страницу товара.
     */
    @Column(length = 1024)
    private String url;

    /**
     * UNIT — единица измерения: шт., кг и т.д.
     */
    @Column(length = 64)
    private String unit;

    /**
     * SML — размер, вес или объём.
     */
    private String sml;

    /**
     * CAT — категория.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * BAR — штрихкод.
     */
    private String bar;

    @PrePersist
    void prePersist() {
        if (this.publicId == null || this.publicId.isBlank()) {
            this.publicId = UUID.randomUUID().toString().replace("-", "");
        }
    }

}