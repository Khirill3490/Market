package ru.example.productservice.service;


import ru.example.identitydomain.entity.User;

public interface UserService {

    User findByPublicId(String publicId);
}
