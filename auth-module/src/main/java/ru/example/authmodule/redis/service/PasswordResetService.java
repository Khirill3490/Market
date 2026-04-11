package ru.example.authmodule.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.authmodule.exception.IncorrectDataException;
import ru.example.authmodule.mail.MailSenderUtil;
import ru.example.authmodule.redis.repository.RedisTokenRepository;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.authmodule.service.AccountService;
import ru.example.common.util.GenerateToken;
import ru.example.identitydomain.entity.Account;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final RedisTokenRepository tokenRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder encoder;
    private final MailSenderUtil mailSenderUtil;

    @Value("${app.reset.url}")
    private String resetUri;

    private static final String SUBJECT = "Восстановление пароля";

    public void requestReset(String rawEmail) {
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        accountRepository.findByEmailEqualsIgnoreCase(email).ifPresent(user -> {
            String token = GenerateToken.newOpaqueToken();
            String mailBody = getMailBody(token);

            tokenRepository.saveResetToken(token, user.getPublicId());
            mailSenderUtil.sendUri(SUBJECT, mailBody, email);
        });
    }

    public void resetPassword(String token, String newPassword) {
        String publicId = tokenRepository.consumeResetToken(token)
                .orElseThrow(() -> new IncorrectDataException("Ссылка недействительна"));

        Account account = accountService.findByPublicId(publicId);
        account.setPassword(encoder.encode(newPassword));
        accountRepository.save(account);
    }

    private String getMailBody(String token) {
        String msg = "Для восстановления пароля перейдите по ссылке ниже:\n" + resetUri;
        return msg.replace("{token}", token);
    }
}