package ru.example.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "stock_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_reservations_order_product",
                        columnNames = {"order_public_id", "product_public_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_stock_reservations_order_public_id",
                        columnList = "order_public_id"
                ),
                @Index(
                        name = "idx_stock_reservations_status_created_at",
                        columnList = "status, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_public_id", nullable = false, length = 64)
    private String orderPublicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_public_id", nullable = false, length = 64)
    private String productPublicId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = StockReservationStatus.RESERVED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}