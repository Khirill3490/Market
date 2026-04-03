package ru.example.authmodule.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.identitydomain.entity.Company;
import ru.example.identitydomain.entity.User;
import ru.example.identitydomain.entity.enums.RulesType;

@Component
@RequiredArgsConstructor
public class UserAndCompanyMapper {



    public User toUser(UserRegRequest request) {
        return User.builder()
                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .isActive(false)
//                .role(RoleType.ROLE_USER)
//                .company(company)
                .build();
    }

    public Company toCompany(UserRegRequest request) {
        return Company.builder()
                .name(request.getCompanyName())
                .inn(request.getInn())
                .phone(request.getPhone())
                .mail(request.getEmail())
                .city(request.getCity())
                .rules(RulesType.BUYER)
                .build();
    }
}
