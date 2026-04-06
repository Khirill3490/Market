package ru.example.authmodule.service;

import ru.example.authmodule.model.request.AdminUpdateUserRequest;
import ru.example.authmodule.model.response.UserUpdatedResponse;
import ru.example.identitydomain.entity.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    User findByPublicId(String publicId);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);


    UserUpdatedResponse update(String publicId, AdminUpdateUserRequest request);

    void delete(Long id);
}
