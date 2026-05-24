package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.User;

public interface EmailService {

    void sendInitialCredentials(User user, String rawPassword);
}