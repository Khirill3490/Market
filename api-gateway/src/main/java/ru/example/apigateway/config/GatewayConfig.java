package ru.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "auth_route", r -> r.path("/api/v1/auth/**")
                                .uri("lb://auth-module")
                )
                .route(
                        "admin_order_route", r -> r.path(
                                        "/api/v1/admin/orders",
                                        "/api/v1/admin/orders/**"
                                )
                                .uri("lb://user-service")
                )
                .route(
                        "product_route", r -> r.path("/api/v1/products/**")
                                .uri("lb://product-service")
                )
                .route(
                        "user_route", r -> r.path("/api/v1/users/**")
                                .uri("lb://user-service")
                )
                .build();
    }
}