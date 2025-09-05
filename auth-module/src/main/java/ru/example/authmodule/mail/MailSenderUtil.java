package ru.example.authmodule.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSenderUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    public void sendUri(String subject, String messageBody, String toEmail) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        mailSender.send(mailMessage);
    }
}
