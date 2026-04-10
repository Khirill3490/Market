package ru.example.authmodule.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.example.authmodule.exception.ApiErrorResponse;

import java.io.IOException;
import java.time.Instant;

@Component
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException
    ) throws IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Unauthorized")
                .message("Требуется аутентификация")
                .path(request.getRequestURI())
                .validationErrors(null)
                .build();

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
