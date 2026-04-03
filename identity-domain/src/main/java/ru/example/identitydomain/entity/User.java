package ru.example.identitydomain.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.example.identitydomain.entity.enums.RoleType;
import ru.example.identitydomain.entity.enums.RulesType;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String publicId;

    @Column(unique = true)
    private String email;

    private String password;

    private boolean isActive;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    private RulesType rules;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Company company;
}
