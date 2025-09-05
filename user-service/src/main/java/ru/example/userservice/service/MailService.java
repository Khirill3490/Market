package ru.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.example.common.entity.User;
import ru.example.common.exception.ErrorMessageGlobal;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${spring.adminEmail.recipients}")
    private List<String> recipients;

    public void sendMessage(User user) {
        String subject = "Запрос на смену статуса";
        String messageBody = "Пользователь:\npublicId: " + user.getPublicId() + "\nemail: " + user.getEmail()
                + "\nзапрос на смену статуса аккаунта";

        for (String adminEmail : recipients) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(emailFrom);
            mailMessage.setTo(adminEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(messageBody);

            mailSender.send(mailMessage);
        }
    }
}
