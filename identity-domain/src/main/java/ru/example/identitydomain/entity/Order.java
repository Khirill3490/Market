package ru.example.identitydomain.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.example.identitydomain.entity.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_public_id", columnNames = "public_id")
        },
        indexes = {
                @Index(name = "idx_orders_account_id", columnList = "account_id"),
                @Index(name = "idx_orders_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Публичный идентификатор заказа.
     * Его безопаснее отдавать наружу, чем внутренний Long id.
     */
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 64)
    private String publicId;

    /**
     * Аккаунт, которому принадлежит заказ.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Адрес доставки на момент создания заказа.
     * Пока храним ссылку на Address, потому что Address уже принадлежит Account.
     * Позже можно будет добавить address snapshot, чтобы заказ не зависел от изменения адреса.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    /**
     * Статус заказа.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /**
     * Позиции заказа.
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }

        if (status == null) {
            status = OrderStatus.CREATED;
        }

        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}