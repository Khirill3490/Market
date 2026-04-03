//package ru.example.authmodule.security.service;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import ru.example.authmodule.redis.repository.RefreshTokenRepository;
//import ru.example.authmodule.service.UserService;
//
//import ru.example.common.exception.RefreshTokenException;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class RefreshTokenService {
//
//    @Value("${app.jwt.refreshTokenExpiration}")
//    private Long refreshTokenExpiration;
//
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final UserService userService;
//
//    public Optional<RefreshToken> findByRefreshToken(String token) {
//        return refreshTokenRepository.findByToken(token);
//    }
//
//    public String createRefreshToken(String publicId) {
//        var refreshToken = RefreshToken.builder()
//                .userId(userId)
//                .expiryDate(LocalDateTime.now().plusMinutes(refreshTokenExpiration))
//                .token(UUID.randomUUID().toString())
//                .build();
//
//        refreshToken = refreshTokenRepository.save(refreshToken);
//        return refreshToken;
//    }
//
//    public RefreshToken checkRefreshToken(RefreshToken token) {
//        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
//            refreshTokenRepository.delete(token);
//            throw new RefreshTokenException(
//                    token.getToken(), "Refresh token was expired. Repeat signin action!");
//        }
//        return token;
//    }
//
//    public void deleteByUserId(Long userId) {
//        refreshTokenRepository.deleteByUserId(userId);
//    }
//}
