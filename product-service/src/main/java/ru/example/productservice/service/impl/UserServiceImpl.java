package ru.example.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.common.exception.EntityNotFoundException;
import ru.example.identitydomain.entity.Account;
import ru.example.productservice.repository.UserRepository;
import ru.example.productservice.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Account findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId).orElseThrow(() ->
                new EntityNotFoundException("User with id " + publicId + " not found"));
    }
}
