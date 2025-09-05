package ru.example.authmodule.service;



import ru.example.common.entity.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    User findByPublicId(String publicId);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);


    User update(Long id, User user);

    void delete(Long id);
}
