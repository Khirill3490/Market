package ru.example.authmodule.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.Company;
import ru.example.identitydomain.entity.enums.RulesType;

@Component
@RequiredArgsConstructor
public class UserAndCompanyMapper {



    public Account toUser(UserRegRequest request) {
        return Account.builder()
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
