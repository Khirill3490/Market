package ru.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Account;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.IncorrectDataException;
import ru.example.userservice.model.response.CurrentUserResponse;
import ru.example.userservice.repository.AccountRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private final AccountRepository accountRepository;

    public CurrentUserResponse getCurrentUser(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);

        Account account = accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Локальный account для текущего пользователя не найден"
                ));

        return CurrentUserResponse.builder()
                .accountPublicId(account.getPublicId())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                .accountStatus(account.getStatus() != null ? account.getStatus().name() : null)
                .build();
    }

    private String requireKeycloakUserId(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        return keycloakUserId;
    }
}