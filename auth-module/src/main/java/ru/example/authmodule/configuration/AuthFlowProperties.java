package ru.example.authmodule.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthFlowProperties(
        boolean legacyLocalAuthEnabled
) {
}