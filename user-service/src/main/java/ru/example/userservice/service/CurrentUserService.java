package ru.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Account;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.IncorrectDataException;
import ru.example.userservice.model.request.UpdateCurrentUserRequest;
import ru.example.userservice.model.response.CurrentUserResponse;
import ru.example.userservice.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Jwt jwt) {
        Account account = getCurrentAccount(jwt);

        return getCurrentUserResponse(account);
    }

    @Transactional
    public CurrentUserResponse updateCurrentUser(Jwt jwt, UpdateCurrentUserRequest request) {
        Account account = getCurrentAccount(jwt);

        if (request.firstName() != null) {
            account.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            account.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            account.setPhone(request.phone());
        }

        accountRepository.save(account);

        return getCurrentUserResponse(account);
    }

    private String requireKeycloakUserId(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        return keycloakUserId;
    }

    private CurrentUserResponse getCurrentUserResponse(Account account) {
        return CurrentUserResponse.builder()
                .accountPublicId(account.getPublicId())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .phone(account.getPhone())
                .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                .accountStatus(account.getStatus() != null ? account.getStatus().name() : null)
                .build();
    }

    private Account getCurrentAccount(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);

        return accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Локальный account для текущего пользователя не найден"
                ));
    }
}