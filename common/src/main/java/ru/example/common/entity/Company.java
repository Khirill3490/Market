package ru.example.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.common.entity.enums.RulesType;


@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String inn;

    private String city;
    private String phone;
    private String mail;

//    @Column(nullable = false)
//    private String password;

    private String url;

    @Enumerated(EnumType.STRING)
    private RulesType rules;

    private String logo;

    private Double tax;

    private Double rating;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User owner;
    
}

