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
import ru.example.authmodule.repository.CompanyRepository;
import ru.example.authmodule.repository.UserRepository;
import ru.example.authmodule.security.AppUserPrincipal;
import ru.example.authmodule.security.jwt.JwtUtils;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.UserService;
import ru.example.common.entity.Company;
import ru.example.common.entity.RefreshToken;
import ru.example.common.entity.User;
import ru.example.common.entity.enums.RoleType;
import ru.example.common.entity.enums.RulesType;
import ru.example.common.exception.ErrorMessageGlobal;
import ru.example.common.exception.IncorrectDataException;
import ru.example.common.exception.RefreshTokenException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
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

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());


        return AuthResponse.builder()
                .accessToken(jwtUtils.generateJwtToken(userPrincipal))
                .refreshToken(refreshToken.getToken())
                .email(userPrincipal.getUsername())
                .role(role)
                .build();
    }

    public void register(UserRegRequest request) {
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
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByRefreshToken(requestRefreshToken)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User tokenOwner = userRepository.findById(userId).orElseThrow(() ->
                            new RefreshTokenException("Exception trying to get token for userId: " + userId));

                    String token = jwtUtils.generateJwtToken(new AppUserPrincipal(tokenOwner));

                    return new RefreshTokenResponse(
                            token,
                            refreshTokenService.createRefreshToken(userId).getToken()
                    );
                }).orElseThrow(() -> new RefreshTokenException(requestRefreshToken, "Refresh token not found"));
    }

    public void logout() {
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentPrincipal instanceof AppUserPrincipal userDetails) {
            Long userId = userDetails.getId();

            refreshTokenService.deleteByUserId(userId);
        }
    }

    private AppUserPrincipal toUserPrincipal(User user) {
        return new AppUserPrincipal(user);
    }
}
