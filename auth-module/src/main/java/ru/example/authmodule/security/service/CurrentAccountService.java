package ru.example.authmodule.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.authmodule.exception.EntityAlreadyExistsException;
import ru.example.authmodule.exception.IncorrectDataException;
import ru.example.authmodule.model.response.CurrentAccountResponse;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.authmodule.service.AccountService;
import ru.example.identitydomain.entity.Account;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentAccountService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @Transactional
    public CurrentAccountResponse getCurrentAccount(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);
        String email = normalizeEmail(jwt.getClaimAsString("email"));
        String firstName = blankToNull(jwt.getClaimAsString("given_name"));
        String lastName = blankToNull(jwt.getClaimAsString("family_name"));

        Account account = accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> createAccountIfNeeded(
                        keycloakUserId,
                        email,
                        firstName,
                        lastName
                ));

        return CurrentAccountResponse.builder()
                .keycloakUserId(account.getKeycloakUserId())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .localAccountExists(true)
                .accountPublicId(account.getPublicId())
                .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                .accountStatus(account.getStatus() != null ? account.getStatus().name() : null)
                .build();
    }

    private Account createAccountIfNeeded(
            String keycloakUserId,
            String email,
            String firstName,
            String lastName
    ) {
        if (email == null || email.isBlank()) {
            throw new IncorrectDataException(
                    "В JWT отсутствует email, поэтому невозможно создать локальный account"
            );
        }

        Optional<Account> accountWithSameEmail = accountRepository.findByEmailEqualsIgnoreCase(email);
        if (accountWithSameEmail.isPresent()) {
            Account found = accountWithSameEmail.get();

            if (found.getKeycloakUserId() == null || !keycloakUserId.equals(found.getKeycloakUserId())) {
                throw new EntityAlreadyExistsException(
                        "Локальный account с таким email уже существует и привязан к другому пользователю"
                );
            }

            return found;
        }

        return accountService.createLocalAccount(
                keycloakUserId,
                email,
                firstName,
                lastName
        );
    }

    private String requireKeycloakUserId(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        return keycloakUserId;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}