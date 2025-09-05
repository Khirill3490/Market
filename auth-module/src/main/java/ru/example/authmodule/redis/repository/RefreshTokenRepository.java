package ru.example.authmodule.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redis;

    @Value("${app.auth.key.refresh-prefix}")
    private String rtPrefix;

    @Value("${app.auth.refresh-ttl}")
    private Duration refreshTtl;

    private String refreshTokenKey(String refresh) {
        return rtPrefix + refresh;
    }

    /** Сохранить refresh -> publicId */
    public void save(String refresh, String publicId) {
        redis.opsForValue().set(refreshTokenKey(refresh), publicId, refreshTtl);
    }

    /** Проверить, что refresh существует */
    public boolean exists(String refresh) {
        return Boolean.TRUE.equals(redis.hasKey(refreshTokenKey(refresh)));
    }

    /** Получить publicId */
    public Optional<String> getPublicId(String refresh) {
        return Optional.ofNullable(redis.opsForValue().get(refreshTokenKey(refresh)));
    }

    /** Удалить refresh */
    public void delete(String refresh) {
        redis.delete(refreshTokenKey(refresh));
    }

    /** Ротация: old -> new, если old существует */
    public boolean rotate(String oldRefresh, String newRefresh) {
        String oldKey = refreshTokenKey(oldRefresh);
        String val = redis.opsForValue().get(oldKey);
        if (val == null) return false;
        redis.delete(oldKey);
        redis.opsForValue().set(refreshTokenKey(newRefresh), val, refreshTtl);
        return true;
    }
}

