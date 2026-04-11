package ru.example.userservice.mapper;

import org.springframework.stereotype.Component;

import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.Company;
import ru.example.userservice.model.response.AccountResponse;

@Component
public class AccountMapper {

    public AccountResponse toAccountResponse(Account account, Company company) {
        return AccountResponse.builder()
                .username(account.getEmail())
                .isActive(account.isActive())
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
