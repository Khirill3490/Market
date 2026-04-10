package ru.example.authmodule.security.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.authmodule.exception.*;
import ru.example.authmodule.mapper.UserAndCompanyMapper;
import ru.example.authmodule.model.request.LoginRequest;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.authmodule.model.request.RefreshTokenRequest;
import ru.example.authmodule.model.response.AuthResponse;
import ru.example.authmodule.model.response.RefreshTokenResponse;
import ru.example.authmodule.redis.repository.RefreshTokenRepository;
import ru.example.authmodule.repository.CompanyRepository;
import ru.example.authmodule.repository.UserRepository;
import ru.example.authmodule.security.AppUserPrincipal;
import ru.example.authmodule.security.jwt.JwtUtils;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.UserService;
import ru.example.common.util.GenerateToken;
import ru.example.identitydomain.entity.Company;
import ru.example.identitydomain.entity.User;
import ru.example.identitydomain.entity.enums.RoleType;
import ru.example.identitydomain.entity.enums.RulesType;
import ru.example.authmodule.configuration.AuthFlowProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import ru.example.authmodule.model.response.CurrentUserResponse;

import java.util.List;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;
    private final UserAndCompanyMapper userAndCompanyMapper;
    private final UserService userService;
    private final AuthFlowProperties authFlowProperties;


    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        ensureLegacyLocalAuthEnabled();

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUserPrincipal userPrincipal = (AppUserPrincipal) authentication.getPrincipal();
        String role = userPrincipal.getAuthorities().toString();

        String refreshToken = GenerateToken.newOpaqueToken();
        refreshTokenRepository.save(refreshToken, userPrincipal.getPublicId());


        return AuthResponse.builder()
                .accessToken(jwtUtils.generateJwtToken(userPrincipal))
                .refreshToken(refreshToken)
                .email(userPrincipal.getUsername())
                .role(role)
                .build();
    }

    public void register(UserRegRequest request) {
        if (companyRepository.existsByInn(request.getInn())) {
            throw new EntityAlreadyExistsException("Компания с данным ИНН уже зарегистрирована");
        }

        Optional<User> userOptional = userRepository.findByEmailEqualsIgnoreCase(request.getEmail());
        String inn = request.getInn();

        if (companyRepository.existsByInn(inn)) {
            throw new EntityAlreadyExistsException("Данный ИНН уже зарегистрирован");
        }

        if (userOptional.isPresent()) {
            User userDb = userOptional.get();
            if (userDb.isActive()) {
                throw new EntityAlreadyExistsException(
                        "Ошибка. Пользователь с email: " + request.getEmail() + " уже существует"
                );
            }
            throw new ErrorMessageGlobal("Сообщение с ссылкой для подтверждения аккаунта уже было отправлено на email");
        }

        Company company = userAndCompanyMapper.toCompany(request);



        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .role(RoleType.ROLE_USER)
                .rules(RulesType.BUYER)
                .company(company)
                .build();

        company.setOwner(user);
        User savedUser = userService.save(user);

        activationService.sendUri(savedUser.getPublicId(), savedUser.getEmail());
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        ensureLegacyLocalAuthEnabled();

        String oldRefreshToken = request.getRefreshToken();

        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            throw new RefreshTokenException("Refresh token is blank");
        }

        String publicId = refreshTokenRepository.getPublicId(oldRefreshToken)
                .orElseThrow(() -> new RefreshTokenException(
                        oldRefreshToken,
                        "Refresh token not found or expired"
                ));

        User tokenOwner = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RefreshTokenException(
                        oldRefreshToken,
                        "User for refresh token not found"
                ));

        if (!tokenOwner.isActive()) {
            refreshTokenRepository.delete(oldRefreshToken);
            throw new RefreshTokenException(
                    oldRefreshToken,
                    "User is not active"
            );
        }

        String newRefreshToken = GenerateToken.newOpaqueToken();

        boolean rotated = refreshTokenRepository.rotate(oldRefreshToken, newRefreshToken);
        if (!rotated) {
            throw new RefreshTokenException(
                    oldRefreshToken,
                    "Refresh token was already used or removed"
            );
        }

        String newAccessToken = jwtUtils.generateJwtToken(toUserPrincipal(tokenOwner));

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    private AppUserPrincipal toUserPrincipal(User user) {
        return new AppUserPrincipal(user);
    }

    public void logout(RefreshTokenRequest request) {
        ensureLegacyLocalAuthEnabled();

        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenException("Refresh token is blank");
        }

        refreshTokenRepository.delete(refreshToken);
    }

    private void ensureLegacyLocalAuthEnabled() {
        if (!authFlowProperties.legacyLocalAuthEnabled()) {
            throw new LegacyAuthFlowDisabledException(
                    "Локальный flow аутентификации отключен. Используйте вход через Keycloak."
            );
        }
    }


    public CurrentUserResponse getCurrentUser(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        String email = extractEmail(jwt);
        String username = extractUsername(jwt);

        List<String> authorities = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Optional<User> localUserOptional = resolveAndSyncLocalUser(jwt);

        if (localUserOptional.isEmpty()) {
            return CurrentUserResponse.builder()
                    .keycloakUserId(keycloakUserId)
                    .username(username)
                    .email(email)
                    .authorities(authorities)
                    .localUserExists(false)
                    .build();
        }

        User localUser = localUserOptional.get();

        return CurrentUserResponse.builder()
                .keycloakUserId(keycloakUserId)
                .username(username)
                .email(email)
                .authorities(authorities)
                .localUserExists(true)
                .localPublicId(localUser.getPublicId())
                .localRole(localUser.getRole() != null ? localUser.getRole().name() : null)
                .localActive(localUser.isActive())
                .build();
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

    private Optional<User> resolveAndSyncLocalUser(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        Optional<User> byKeycloakId = userRepository.findByKeycloakUserId(keycloakUserId);
        if (byKeycloakId.isPresent()) {
            return byKeycloakId;
        }

        String email = extractEmail(jwt);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        Optional<User> byEmail = userRepository.findByEmailEqualsIgnoreCase(email);
        if (byEmail.isEmpty()) {
            return Optional.empty();
        }

        User localUser = byEmail.get();

        if (localUser.getKeycloakUserId() != null
                && !localUser.getKeycloakUserId().equals(keycloakUserId)) {
            throw new EntityAlreadyExistsException(
                    "Локальный пользователь уже привязан к другому Keycloak аккаунту"
            );
        }

        localUser.setKeycloakUserId(keycloakUserId);
        User savedUser = userRepository.save(localUser);

        return Optional.of(savedUser);
    }

}
