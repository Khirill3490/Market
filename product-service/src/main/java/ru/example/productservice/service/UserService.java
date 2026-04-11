package ru.example.productservice.service;


import ru.example.identitydomain.entity.Account;

public interface UserService {

    Account findByPublicId(String publicId);
}
