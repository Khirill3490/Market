package ru.example.authmodule.security.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.authmodule.exception.EntityAlreadyExistsException;
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
import ru.example.common.exception.ErrorMessageGlobal;
import ru.example.authmodule.exception.RefreshTokenException;
import ru.example.common.util.GenerateToken;
import ru.example.identitydomain.entity.Company;
import ru.example.identitydomain.entity.User;
import ru.example.identitydomain.entity.enums.RoleType;
import ru.example.identitydomain.entity.enums.RulesType;

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


    public AuthResponse authenticateUser(LoginRequest loginRequest) {
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
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenException("Refresh token is blank");
        }

        refreshTokenRepository.delete(refreshToken);
    }

}
