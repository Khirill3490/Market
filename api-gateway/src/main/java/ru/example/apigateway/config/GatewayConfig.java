package ru.example.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.example.apigateway.security.AuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "auth_route", r -> r.path("/api/v1/auth/**")
                                .filters(f -> f.filter(filter))
                                .uri("lb://auth-module")
                )
                .route(
                        "product_route", r -> r.path("/api/v1/products/**")
                                .uri("lb://product-service")
                )
                .route(
                        "user_route", r -> r.path("/api/v1/user/**")
                                .filters(f -> f.filter(filter))
                                .uri("lb://user-service")
                )
                .route("frontend_route", r -> r.path("/",
                                "/index", "/catalog", "/register", "/login", "/activation", "/account", "/assets/**",
                                "/product")
                        .uri("lb://frontend") // прямой адрес сервиса
                )
                .build();
    }
}
