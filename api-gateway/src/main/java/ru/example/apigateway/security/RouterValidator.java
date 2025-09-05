package ru.example.apigateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    // Список незащищённых путей (разрешённых без токена)
    private static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth",
            "/api/v1/public"
    );

    // Проверка: если запрос начинается с одного из разрешённых — он не защищён
    public static final Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints.stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));

//    public static final Predicate<ServerHttpRequest> isSecured =
//            request -> !request.getURI().getPath().startsWith("/api/v1/auth");
}
