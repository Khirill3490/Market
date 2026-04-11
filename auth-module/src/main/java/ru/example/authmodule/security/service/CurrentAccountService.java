package ru.example.authmodule.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.authmodule.exception.IncorrectDataException;
import ru.example.authmodule.model.response.CurrentAccountResponse;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.authmodule.service.AccountService;
import ru.example.identitydomain.entity.Account;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentAccountService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public CurrentAccountResponse getCurrentAccount(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);
        String email = normalizeEmail(extractEmail(jwt));
        String username = extractUsername(jwt);

        Optional<Account> accountOptional = resolveAccount(keycloakUserId, email);

        return buildResponse(
                keycloakUserId,
                username,
                email,
                accountOptional.orElse(null)
        );
    }

    @Transactional
    public Account getOrCreateCurrentAccount(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);
        String email = normalizeEmail(extractEmail(jwt));

        Optional<Account> existing = resolveAccount(keycloakUserId, email);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (email == null || email.isBlank()) {
            throw new IncorrectDataException(
                    "В JWT отсутствует email, поэтому невозможно создать локальный account"
            );
        }

        String firstName = extractFirstName(jwt);
        String lastName = extractLastName(jwt);

        return accountService.createShellAccount(
                keycloakUserId,
                email,
                firstName,
                lastName
        );
    }

    private Optional<Account> resolveAccount(String keycloakUserId, String email) {
        Optional<Account> byKeycloakId = accountRepository.findByKeycloakUserId(keycloakUserId);
        if (byKeycloakId.isPresent()) {
            return byKeycloakId;
        }

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return accountRepository.findByEmailEqualsIgnoreCase(email);
    }

    private String requireKeycloakUserId(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        return keycloakUserId;
    }

    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        return null;
    }

    private String extractUsername(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return jwt.getSubject();
    }

    private String extractFirstName(Jwt jwt) {
        return blankToNull(jwt.getClaimAsString("given_name"));
    }

    private String extractLastName(Jwt jwt) {
        return blankToNull(jwt.getClaimAsString("family_name"));
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

    private CurrentAccountResponse buildResponse(
            String keycloakUserId,
            String username,
            String email,
            Account account
    ) {
        List<String> authorities = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (account == null) {
            return CurrentAccountResponse.builder()
                    .keycloakUserId(keycloakUserId)
                    .username(username)
                    .email(email)
                    .authorities(authorities)
                    .localAccountExists(false)
                    .build();
        }

        return CurrentAccountResponse.builder()
                .keycloakUserId(keycloakUserId)
                .username(username)
                .email(email)
                .authorities(authorities)
                .localAccountExists(true)
                .localPublicId(account.getPublicId())
                .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .companyPublicId(
                        account.getCompany() != null ? account.getCompany().getPublicId() : null
                )
                .build();
    }
}