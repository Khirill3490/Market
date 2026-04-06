package ru.example.authmodule.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redis;

    @Value("${app.auth.key.refresh-prefix}")
    private String rtPrefix;

    @Value("${app.auth.refresh-ttl}")
    private Duration refreshTtl;

    private final DefaultRedisScript<Long> rotateScript = new DefaultRedisScript<>(
            """
            local oldKey = KEYS[1]
            local newKey = KEYS[2]
            local ttlMillis = ARGV[1]
    
            local value = redis.call('GET', oldKey)
            if not value then
                return 0
            end
    
            redis.call('DEL', oldKey)
            redis.call('SET', newKey, value, 'PX', ttlMillis)
    
            return 1
            """,
            Long.class
    );

    private String refreshTokenKey(String refresh) {
        return rtPrefix + refresh;
    }

    public void save(String refresh, String publicId) {
        redis.opsForValue().set(refreshTokenKey(refresh), publicId, refreshTtl);
    }

    public boolean exists(String refresh) {
        return Boolean.TRUE.equals(redis.hasKey(refreshTokenKey(refresh)));
    }

    public Optional<String> getPublicId(String refresh) {
        return Optional.ofNullable(redis.opsForValue().get(refreshTokenKey(refresh)));
    }

    public void delete(String refresh) {
        redis.delete(refreshTokenKey(refresh));
    }

    public boolean rotate(String oldRefresh, String newRefresh) {
        String oldKey = refreshTokenKey(oldRefresh);
        String newKey = refreshTokenKey(newRefresh);

        Long result = redis.execute(
                rotateScript,
                List.of(oldKey, newKey),
                String.valueOf(refreshTtl.toMillis())
        );

        return Long.valueOf(1L).equals(result);
    }
}