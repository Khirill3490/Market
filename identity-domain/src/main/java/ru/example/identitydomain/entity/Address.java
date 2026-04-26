package ru.example.identitydomain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "addresses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_addresses_public_id", columnNames = "public_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, length = 64)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_addresses_account_id")
    )
    private Account account;

    @Column(name = "label", length = 100)
    private String label;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "house", nullable = false, length = 32)
    private String house;

    @Column(name = "apartment", length = 32)
    private String apartment;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "comment", length = 255)
    private String comment;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
