package ru.example.identitydomain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_product_public_id", columnList = "product_public_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заказ, которому принадлежит позиция.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Идентификатор товара из product-service/catalog context.
     * Не делаем JPA-связь на Product.
     */
    @Column(name = "product_public_id", nullable = false, length = 64)
    private String productPublicId;

    /**
     * Название товара на момент создания заказа.
     * Это snapshot.
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Картинка товара на момент создания заказа.
     */
    @Column(name = "product_image", length = 1024)
    private String productImage;

    /**
     * Количество единиц товара.
     */
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
}