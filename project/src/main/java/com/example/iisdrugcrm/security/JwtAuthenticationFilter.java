package com.example.iisdrugcrm.security;

import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, TokenBlacklistService tokenBlacklistService, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (tokenBlacklistService.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                return;
            }

            if (tokenProvider.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = tokenProvider.getUsername(token);
                boolean passwordChangeRequired = userRepository.findByUsername(username)
                        .map(user -> !user.isHasChangedPassword())
                        .orElse(true);

                if (requiresPasswordChange(request) && passwordChangeRequired) {
                    writePasswordChangeRequired(response);
                    return;
                }

                List<SimpleGrantedAuthority> authorities = tokenProvider.getRoles(token).stream()
                        .map(UserRole::name)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresPasswordChange(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/api/auth/login")
                && !path.equals("/api/auth/logout")
                && !path.equals("/api/users/profile/password");
    }

    private void writePasswordChangeRequired(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"errorCode\":\"PASSWORD_CHANGE_REQUIRED\",\"error\":\"Password change required\"}");
    }
}