package ru.example.authmodule.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "keycloak",
        type = SecuritySchemeType.OAUTH2,
        in = SecuritySchemeIn.HEADER,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://localhost:8088/realms/market/protocol/openid-connect/auth",
                        tokenUrl = "http://localhost:8088/realms/market/protocol/openid-connect/token"
                )
        )
)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .addSecurityItem(
                        new io.swagger.v3.oas.models.security.SecurityRequirement().addList("keycloak")
                );
    }
}