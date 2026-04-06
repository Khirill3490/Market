package ru.example.authmodule.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.authmodule.mail.MailSenderUtil;
import ru.example.authmodule.redis.repository.RedisTokenRepository;
import ru.example.authmodule.repository.UserRepository;
import ru.example.authmodule.service.UserService;
import ru.example.common.exception.IncorrectDataException;
import ru.example.common.util.GenerateToken;
import ru.example.identitydomain.entity.User;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final RedisTokenRepository tokenRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final MailSenderUtil mailSenderUtil;

    @Value("${app.reset.url}")
    private String resetUri;

    private static final String SUBJECT = "Восстановление пароля";

    public void requestReset(String rawEmail) {
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmailEqualsIgnoreCase(email).ifPresent(user -> {
            String token = GenerateToken.newOpaqueToken();
            String mailBody = getMailBody(token);

            tokenRepository.saveResetToken(token, user.getPublicId());
            mailSenderUtil.sendUri(SUBJECT, mailBody, email);
        });
    }

    public void resetPassword(String token, String newPassword) {
        String publicId = tokenRepository.consumeResetToken(token)
                .orElseThrow(() -> new IncorrectDataException("Ссылка недействительна"));

        User user = userService.findByPublicId(publicId);
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    private String getMailBody(String token) {
        String msg = "Для восстановления пароля перейдите по ссылке ниже:\n" + resetUri;
        return msg.replace("{token}", token);
    }
}