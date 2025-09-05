package ru.example.productservice.service;

import ru.example.common.entity.User;

public interface UserService {

    User findByPublicId(String publicId);
}
