package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.dto.UserCreateDTO;
import com.example.iisdrugcrm.dto.UserResponseDTO;
import com.example.iisdrugcrm.exception.DuplicateUserException;
import com.example.iisdrugcrm.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setActive(true);
        user.setHasChangedPassword(false);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);
        emailService.sendInitialCredentials(savedUser, dto.getPassword());
        return UserResponseDTO.fromEntity(savedUser);
    }

    @Override
    public Page<UserResponseDTO> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponseDTO::fromEntity);
    }
}