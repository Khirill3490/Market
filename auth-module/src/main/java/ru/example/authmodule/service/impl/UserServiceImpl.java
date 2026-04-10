package ru.example.authmodule.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import ru.example.authmodule.exception.EntityAlreadyExistsException;
import ru.example.authmodule.exception.EntityNotFoundException;
import ru.example.authmodule.model.request.AdminUpdateUserRequest;
import ru.example.authmodule.model.response.UserUpdatedResponse;
import ru.example.authmodule.repository.CompanyRepository;
import ru.example.authmodule.repository.UserRepository;
import ru.example.authmodule.service.UserService;
import ru.example.identitydomain.entity.Company;
import ru.example.identitydomain.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
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
    public UserUpdatedResponse update(String publicId, AdminUpdateUserRequest request) {
        User user = findByPublicId(publicId);

        Company company = user.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Компания пользователя не найдена");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            userRepository.findByEmailEqualsIgnoreCase(normalizedEmail)
                    .filter(found -> !found.getPublicId().equals(user.getPublicId()))
                    .ifPresent(found -> {
                        throw new EntityAlreadyExistsException("Пользователь с таким email уже существует");
                    });
            user.setEmail(normalizedEmail);
        }

        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            company.setName(request.getCompanyName().trim());
        }

        if (request.getInn() != null && !request.getInn().isBlank()) {
            companyRepository.findByInn(request.getInn())
                    .filter(found -> !found.getId().equals(company.getId()))
                    .ifPresent(found -> {
                        throw new EntityAlreadyExistsException("Компания с таким ИНН уже существует");
                    });
            company.setInn(request.getInn().trim());
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            company.setCity(request.getCity().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            company.setPhone(request.getPhone().trim());
        }

        if (request.getMail() != null && !request.getMail().isBlank()) {
            company.setMail(request.getMail().trim().toLowerCase());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User savedUser = userRepository.save(user);

        return UserUpdatedResponse.builder()
                .publicId(savedUser.getPublicId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .active(savedUser.isActive())
                .companyName(savedUser.getCompany().getName())
                .inn(savedUser.getCompany().getInn())
                .city(savedUser.getCompany().getCity())
                .phone(savedUser.getCompany().getPhone())
                .mail(savedUser.getCompany().getMail())
                .build();
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
