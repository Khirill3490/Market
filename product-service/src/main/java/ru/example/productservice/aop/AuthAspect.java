package ru.example.productservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.example.common.exception.ErrorMessageGlobal;
import ru.example.identitydomain.entity.User;
import ru.example.productservice.service.UserService;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.myAnnotation.enabled", havingValue = "true")
public class AuthAspect {

    private final HttpServletRequest request;
    private final UserService userService;

    @Around("@annotation(PreAuthorization)")
    public Object checkHeaders(ProceedingJoinPoint joinPoint) throws Throwable {
        String publicId = request.getHeader("publicId");
        String role = request.getHeader("authorities");

//        log.debug("Received headers: publicId={}, role={}", publicId, role);

        if (publicId == null || publicId.isEmpty()) {
            throw new ErrorMessageGlobal("Доступ запрещен");
        }

        // Пример валидации в БД
        User user = userService.findByPublicId(publicId);


        return joinPoint.proceed();
    }
}
