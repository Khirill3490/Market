package ru.example.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final JwtUtils jwtUtils;

//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        log.info("токен1 {}", exchange.getRequest());
//        ServerHttpRequest request = exchange.getRequest();
//        String token = null;
//        if (!isAuthMissing(request)) {
//            String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//            token = this.getAuthHeader(request);
//        }
//
//        if (RouterValidator.isSecured.test(request)) {
//            if (this.isAuthMissing(request)) {
//                return this.onError(exchange);
//            }
//
//            if (jwtUtils.isInvalid(token)) {
//                return this.onError(exchange);
//            }
//        }
//        if (!isAuthMissing(request)) {
//            this.populateRequestWithHeaders(exchange, token);
//        }
//        return chain.filter(exchange);
//    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String token = null;
        if (!isAuthMissing(request)) {
            token = this.getAuthHeader(request);
        }

        if (RouterValidator.isSecured.test(request)) {
            if (this.isAuthMissing(request)) {
                return this.onError(exchange);
            }

            if (jwtUtils.isInvalid(token)) {
                return this.onError(exchange);
            }
        }

        if (!isAuthMissing(request)) {
            exchange = this.populateRequestWithHeaders(exchange, token);
        }

        return chain.filter(exchange);
    }

    protected Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    protected String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").getFirst();
    }

    protected boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    protected ServerWebExchange populateRequestWithHeaders(ServerWebExchange exchange, String tokenString) {
        String jwtToken = tokenString.substring(7);
        Claims tokenClaims = jwtUtils.getAllClaimsFromToken(jwtToken);
        String publicId = jwtUtils.getPublicId(jwtToken);

        Object authoritiesObj = tokenClaims.get("authorities");

        List<String> authorities;
        if (authoritiesObj instanceof List<?>) {
            authorities = ((List<?>) authoritiesObj).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        } else {
            throw new RuntimeException("Поле authorities не является списком");
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("publicId", publicId)
                .header("authorities", authorities.getFirst())
                .build();

        return exchange.mutate().request(mutatedRequest).build();
    }

//    protected void populateRequestWithHeaders(ServerWebExchange exchange, String tokenString) {
//        String jwtToken = tokenString.substring(7);
//        Claims tokenClaims = jwtUtils.getAllClaimsFromToken(jwtToken);
//        String publicId = jwtUtils.getPublicId(jwtToken);
//
//        Object authoritiesObj = tokenClaims.get("authorities");
//
//        List<String> authorities;
//        if (authoritiesObj instanceof List<?>) {
//            authorities = ((List<?>) authoritiesObj).stream()
//                    .filter(Objects::nonNull)
//                    .map(Object::toString) // на случай, если там не строки
//                    .toList();
//        } else {
//            throw new RuntimeException("Поле authorities не является списком");
//        }
//
//        exchange.getRequest().mutate()
//                .header("publicId", publicId)
//                .header("authorities", authorities.getFirst())
//                .build();
//    }


}
