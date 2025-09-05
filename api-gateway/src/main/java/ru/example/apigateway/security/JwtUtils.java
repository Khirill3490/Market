package ru.example.apigateway.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static ru.example.apigateway.security.SecurityConstants.TOKEN_PREFIX;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private final SecretKey secretKey;

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(String tokenString) {
        log.info("токен {}", tokenString);
        String jwtToken = tokenString.replace(TOKEN_PREFIX, "");
        log.info("токен {}", jwtToken);
        boolean result = true;
        try {
            result = getAllClaimsFromToken(jwtToken)
                    .getExpiration()
                    .before(new Date());
        } catch (Exception ex) {
            log.info("Ошибка jwt верификации: {}", ex.getMessage());
        }
        return result;
    }

    public boolean isInvalid(String token) {
        return isTokenExpired(token);
    }

    public String getPublicId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
