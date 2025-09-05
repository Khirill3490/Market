package ru.example.authmodule.security.jwt;


import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.example.authmodule.security.AppUserPrincipal;
import ru.example.common.exception.ErrorMessageGlobal;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    private final SecretKey secretKey;

    public String generateJwtToken(AppUserPrincipal userDetails) {
        long now = System.currentTimeMillis();
//        String username = userDetails.getUsername();
        String publicId = userDetails.getPublicId();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(publicId)
                .claim("authorities", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(now + tokenExpiration.toMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateActivationToken(String publicId) {
        return Jwts.builder()
                .setSubject(publicId)
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration.toMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

//    public String generateJwtTokenByOwner(User user) {
//        long now = System.currentTimeMillis();
//        String username = user.getEmail()
//        Long id = userDetails.getId();
//        String role = userDetails.getAuthorities().toString();
//
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("id", id)
//                .claim("authorities", role)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(now + tokenExpiration.toMillis()))
//                .signWith(secretKey, SignatureAlgorithm.HS512)
//                .compact();
//    }


//    public String getUsername(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }

    public String getPublicId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validate(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Недопустимая подпись JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Срок действия JWT токена истёк: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Срок действия токена истёк");
        } catch (UnsupportedJwtException e) {
            log.error("Неподдерживаемый формат JWT токена: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Пустая строка с JWT токеном или недопустимые claims: {}", e.getMessage());
        }
        return false;
    }


//    public boolean validate(String authToken) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(secretKey)
//                    .build()
//                    .parseClaimsJws(authToken);
//            return true;
//        } catch (SecurityException | MalformedJwtException e) {
//            log.error("Invalid JWT signature: {}", e.getMessage());
//        } catch (ExpiredJwtException e) {
//            log.warn("JWT token is expired: {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            log.error("Unsupported JWT token: {}", e.getMessage());
//        } catch (IllegalArgumentException e) {
//            log.error("JWT claims string is empty: {}", e.getMessage());
//        }
//        return false;
//    }

}
