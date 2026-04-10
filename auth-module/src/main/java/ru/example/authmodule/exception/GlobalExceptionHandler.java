package ru.example.authmodule.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(IncorrectDataException.class)
    public ResponseEntity<ApiErrorResponse> handleIncorrectData(
            IncorrectDataException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(ErrorMessageGlobal.class)
    public ResponseEntity<ApiErrorResponse> handleErrorMessageGlobal(
            ErrorMessageGlobal ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityAlreadyExists(
            EntityAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenException(
            RefreshTokenException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        BindingResult bindingResult = ex.getBindingResult();

        Map<String, String> validationErrors = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации запроса",
                request,
                validationErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Недостаточно прав для выполнения операции",
                request,
                null
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректный формат тела запроса",
                request,
                null
        );
    }
}