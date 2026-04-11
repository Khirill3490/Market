package ru.example.authmodule.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.authmodule.exception.IncorrectDataException;
import ru.example.authmodule.mail.MailSenderUtil;
import ru.example.authmodule.redis.repository.RedisTokenRepository;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.AccountService;
import ru.example.common.util.GenerateToken;
import ru.example.identitydomain.entity.Account;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationServiceImpl implements ActivationService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final RedisTokenRepository tokenRepository;
    private final MailSenderUtil mailSenderUtil;


    @Value("${app.activation.url}")
    private String activationUri;

    @Override
    public void sendUri(String publicId, String rawEmail) {
        String subject = "Активация аккаунта";
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        String token = GenerateToken.newOpaqueToken();
        String mailBody = getMailBody(token);

        tokenRepository.saveActivationToken(token, publicId);

        log.info("Activation: перед отправкой на {}", email);
        mailSenderUtil.sendUri(subject, mailBody, email);
        log.info("MAIL: после отправки на {}", email);
    }

    @Override
    public void activate(String token) {
        String publicId = tokenRepository.consumeActivationToken(token)
                .orElseThrow(() -> new IncorrectDataException("Ссылка недействительна"));

        Account account = accountService.findByPublicId(publicId);

        account.setActive(true);
        accountRepository.save(account);
    }

    private String getMailBody(String token) {
        String msg  = "Для завершения регистрации перейдите по ссылке ниже:\n " + activationUri;
        return msg.replace("{token}", token);
    }
}
