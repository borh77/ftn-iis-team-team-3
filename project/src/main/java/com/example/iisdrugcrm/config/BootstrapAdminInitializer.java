package com.example.iisdrugcrm.config;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public BootstrapAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap-admin.username:admin}") String username,
            @Value("${app.security.bootstrap-admin.email:admin@local.dev}") String email,
            @Value("${app.security.bootstrap-admin.password}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setRole(UserRole.ROLE_ADMIN);
        admin.setActive(true);
        admin.setHasChangedPassword(false);
        admin.setPasswordHash(passwordEncoder.encode(password));

        userRepository.save(admin);
    }
}