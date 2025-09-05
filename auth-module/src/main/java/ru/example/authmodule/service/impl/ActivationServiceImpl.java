package ru.example.authmodule.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.example.authmodule.mail.MailSenderUtil;
import ru.example.authmodule.redis.repository.RedisTokenRepository;
import ru.example.authmodule.repository.UserRepository;
import ru.example.authmodule.security.jwt.JwtUtils;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.UserService;
import ru.example.common.entity.User;
import ru.example.common.exception.ErrorMessageGlobal;
import ru.example.common.exception.IncorrectDataException;
import ru.example.common.util.GenerateToken;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ActivationServiceImpl implements ActivationService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RedisTokenRepository tokenRepository;
    private final MailSenderUtil mailSenderUtil;


    @Value("${app.activation.url}")
    private String activationUri;

    @Override
    public void sendUri(String publicId, String rawEmail) {
        String subject = "Активация аккаунта";
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmailEqualsIgnoreCase(email).ifPresent(user -> {
            // 1) Сгенерить токен
            String token = GenerateToken.newOpaqueToken();
            String mailBody = getMailBody(token);
            String key = tokenRepository.getActivationKey(token);

            // 2) Сохранить в Redis
            tokenRepository.save(key, user.getPublicId());
            // 3) Отправить письмо
            mailSenderUtil.sendUri(subject, mailBody, email);
        });

    }

//    @Override
//    public void activate(String token) {
//        if (!jwtUtils.validate(token)) {
//            throw new ErrorMessageGlobal("Ошибка. Ссылка некорректна");
//        }
//
//        String publicId = jwtUtils.getPublicId(token);
//        User userFromDb = userService.findByPublicId(publicId);
//
//        if (userFromDb.isActive()) {
//            throw new ErrorMessageGlobal("Срок действия ссылки истёк. Аккаунт уже активирован");
//        }
//
//        userFromDb.setActive(true);
//        userRepository.save(userFromDb);
//    }

    public void activate(String token) {
        String publicId = tokenRepository.consume(token)
                .orElseThrow(() -> new IncorrectDataException("Ссылка недействительна"));
        User user = userService.findByPublicId(publicId);

        user.setActive(true);
        userRepository.save(user);
    }

    private String getMailBody(String token) {
        String msg  = "Для завершения регистрации перейдите по ссылке ниже:\n " + activationUri;
        return msg.replace("{token}", token);
    }
}
