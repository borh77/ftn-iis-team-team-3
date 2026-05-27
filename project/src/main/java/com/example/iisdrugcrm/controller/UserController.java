package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.UserCreateDTO;
import com.example.iisdrugcrm.dto.UserResponseDTO;
import com.example.iisdrugcrm.dto.auth.LoginResponseDTO;
import com.example.iisdrugcrm.dto.profile.PasswordChangeDTO;
import com.example.iisdrugcrm.dto.profile.ProfileUpdateResponseDTO;
import com.example.iisdrugcrm.dto.profile.UserUpdateDTO;
import com.example.iisdrugcrm.dto.team.TeamMemberDTO;
import com.example.iisdrugcrm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "role",
            "isActive",
            "hasChangedPassword"
    );

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('PRICELIST_CREATOR')")
    public ResponseEntity<List<TeamMemberDTO>> searchPricelistCreators(@RequestParam(defaultValue = "") String username) {
        return ResponseEntity.ok(userService.searchPricelistCreators(username));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileUpdateResponseDTO> updateProfile(Authentication authentication, @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(authentication.getName(), dto));
    }

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoginResponseDTO> changePassword(Authentication authentication, @Valid @RequestBody PasswordChangeDTO dto) {
        return ResponseEntity.ok(userService.changePassword(authentication.getName(), dto));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String rawDirection = parts[1].trim().toUpperCase(Locale.ROOT);
            direction = Sort.Direction.fromString(rawDirection);
        }

        return Sort.by(direction, field);
    }
}