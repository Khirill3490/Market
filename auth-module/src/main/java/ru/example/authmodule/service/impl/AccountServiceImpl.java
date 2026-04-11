package ru.example.authmodule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.authmodule.service.AccountService;
import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.enums.AccountStatus;
import ru.example.identitydomain.entity.enums.AccountType;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Account createShellAccount(
            String keycloakUserId,
            String email,
            String firstName,
            String lastName
    ) {
        Account account = Account.builder()
                .publicId(generatePublicId())
                .keycloakUserId(keycloakUserId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .accountType(AccountType.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(account);
    }

    private String generatePublicId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}