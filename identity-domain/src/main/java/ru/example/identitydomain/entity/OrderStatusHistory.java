package ru.example.identitydomain.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.example.identitydomain.entity.enums.OrderStatus;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "order_status_history",
        indexes = {
                @Index(
                        name = "idx_order_status_history_order_changed_at",
                        columnList = "order_id, changed_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 50)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    private OrderStatus toStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = OffsetDateTime.now();
        }
    }
}