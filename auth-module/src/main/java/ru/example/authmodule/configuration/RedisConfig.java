package ru.example.authmodule.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    // Для строковых ключей/значений (идеально для токенов/TTL)
    @Bean
    public StringRedisTemplate stringRedisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    // Для объектов (если нужно)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory cf) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(cf);

        var keySer = new org.springframework.data.redis.serializer.StringRedisSerializer();
        var valSer = new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySer);
        template.setHashKeySerializer(keySer);
        template.setValueSerializer(valSer);
        template.setHashValueSerializer(valSer);
        template.afterPropertiesSet();
        return template;
    }
}

