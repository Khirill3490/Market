package ru.example.userservice.mapper;

import org.springframework.stereotype.Component;
import ru.example.common.entity.Company;
import ru.example.common.entity.User;
import ru.example.userservice.model.response.AccountResponse;

@Component
public class AccountMapper {

    public AccountResponse toAccountResponse(User user, Company company) {
        return AccountResponse.builder()
                .username(user.getEmail())
                .isActive(user.isActive())
                .companyName(company.getName())
                .inn(company.getInn())
                .city(company.getCity())
                .phone(company.getPhone())
                .mail(company.getMail())
                .url(company.getUrl())
                .rules(company.getRules().toString())
                .logo(company.getLogo())
                .tax(company.getTax())
                .rating(company.getRating())
                .build();
    }


}
