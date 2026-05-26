package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.dto.auth.LoginRequestDTO;
import com.example.iisdrugcrm.dto.auth.LoginResponseDTO;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.security.TokenBlacklistService;
import com.example.iisdrugcrm.security.JwtTokenProvider;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            JwtTokenProvider tokenProvider,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        return buildResponse(
                user.getUsername(),
                List.of(user.getRole()),
                user.isActive(),
                user.isHasChangedPassword()
        );
    }

    private LoginResponseDTO buildResponse(String username, List<UserRole> roles, boolean active, boolean hasChangedPassword) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsername(username);
        response.setRoles(roles);
        response.setActive(active);
        response.setHasChangedPassword(hasChangedPassword);
        response.setToken(tokenProvider.generateToken(username, roles));
        return response;
    }

    @Override
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authorizationHeader.substring(7);
        tokenBlacklistService.blacklist(token, tokenProvider.getExpiration(token));
    }
}