package ru.example.userservice.service;

import ru.example.common.dto.SimpleMessageResponse;
import ru.example.userservice.model.response.AccountResponse;

public interface AccountService {


    AccountResponse getAccountById(Long id);

    AccountResponse getAccountByPublicId(String publicId);

    SimpleMessageResponse changeUserStatus(String publicId);

}
