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
    private Duration resetTtl;

    @Value("${app.activation.redis.ttl}")
    private Duration activationTtl;

    public String getResetKey(String token) {
        return resetKeyPrefix + token;
    }

    public String getActivationKey(String token) {
        return activationKeyPrefix + token;
    }

    public void saveResetToken(String token, String publicId) {
        redis.opsForValue().set(getResetKey(token), publicId, resetTtl);
    }

    public Optional<String> consumeResetToken(String token) {
        String key = getResetKey(token);
        String value = redis.opsForValue().get(key);
        if (value == null) return Optional.empty();
        redis.delete(key);
        return Optional.of(value);
    }

    public void saveActivationToken(String token, String publicId) {
        redis.opsForValue().set(getActivationKey(token), publicId, activationTtl);
    }

    public Optional<String> consumeActivationToken(String token) {
        String key = getActivationKey(token);
        String value = redis.opsForValue().get(key);
        if (value == null) return Optional.empty();
        redis.delete(key);
        return Optional.of(value);
    }

    public void revokeResetToken(String token) {
        redis.delete(getResetKey(token));
    }

    public void revokeActivationToken(String token) {
        redis.delete(getActivationKey(token));
    }
}