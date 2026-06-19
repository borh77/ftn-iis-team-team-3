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

    @Override
    public void sendStatusChangeEmail(
            String toEmail,
            String reporterName,
            Long reportId,
            String oldStatus,
            String newStatus,
            String comment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Adverse effect report status changed");
        message.setText(
                "Hello " + reporterName + ",\n\n" +
                "The status of your adverse effect report #" + reportId + " has changed.\n\n" +
                "Old status: " + oldStatus + "\n" +
                "New status: " + newStatus + "\n" +
                "Comment: " + (comment == null || comment.isBlank() ? "-" : comment) + "\n\n" +
                "Application: " + frontendUrl
        );
        mailSender.send(message);
    }
}
