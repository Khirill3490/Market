package ru.example.authmodule.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import ru.example.authmodule.repository.UserRepository;
import ru.example.authmodule.service.UserService;
import ru.example.common.entity.User;
import ru.example.common.exception.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public User findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId).orElseThrow(() ->
                new EntityNotFoundException("User with id " + publicId + " not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailEqualsIgnoreCase(email).orElseThrow(() ->
                new EntityNotFoundException("User with email '" + email + "' not found"));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        user.setPublicId(getShortUUID());

        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        return null;
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(findById(id));
    }

    private byte[] toByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private String getShortUUID() {
        UUID uuid = UUID.randomUUID();

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(toByteArray(uuid));
    }
}
