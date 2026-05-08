package ru.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "order_outbox_events",
        indexes = {
                @Index(name = "idx_order_outbox_events_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_order_outbox_events_aggregate_id", columnList = "aggregate_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }

        if (status == null) {
            status = OutboxEventStatus.NEW;
        }

        if (attempts == null) {
            attempts = 0;
        }

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}