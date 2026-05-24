package com.example.iisdrugcrm.service.impl;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInitialCredentials(User user, String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your account credentials");
        message.setText(
                "Account created successfully.\n\n" +
                "Application: " + frontendUrl + "\n" +
                "Username: " + user.getUsername() + "\n" +
                "Initial password: " + rawPassword + "\n\n" +
                "You must change your password on first login."
        );
        mailSender.send(message);
    }
}