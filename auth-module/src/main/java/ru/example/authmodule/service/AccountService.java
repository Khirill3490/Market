package ru.example.authmodule.service;


import ru.example.identitydomain.entity.Account;


public interface AccountService {

    Account createLocalAccount(
            String keycloakUserId,
            String email,
            String firstName,
            String lastName
    );
}
