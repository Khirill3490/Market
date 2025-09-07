package ru.example.authmodule.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

    private final StringRedisTemplate redis;

    @Value("${app.reset.redis.key-prefix}")
    private String resetKeyPrefix;

    @Value("${app.activation.redis.key-prefix}")
    private String activationKeyPrefix;

    @Value("${app.reset.redis.ttl}")
    private Duration ttl; // Spring Boot умеет конвертить строки вида 30m, 10s → java.time.Duration

    public String getResetKey(String token) {
        return resetKeyPrefix + token;
    }

    public void save(String token, String publicId) {
        String key = getResetKey(token);
        redis.opsForValue().set(key, publicId, ttl);
    }

    public Optional<String> consume(String token) {
        String k = getResetKey(token);
        String v = redis.opsForValue().get(k);
        if (v == null) return Optional.empty();
        redis.delete(k);
        return Optional.of(v);
    }

    public void revoke(String token) {
        redis.delete(getResetKey(token));
    }

    public boolean exists(String token) {
        return redis.hasKey(getResetKey(token));
    }
}


