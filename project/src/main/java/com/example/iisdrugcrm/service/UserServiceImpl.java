package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.dto.UserCreateDTO;
import com.example.iisdrugcrm.dto.UserResponseDTO;
import com.example.iisdrugcrm.dto.auth.LoginResponseDTO;
import com.example.iisdrugcrm.dto.profile.PasswordChangeDTO;
import com.example.iisdrugcrm.dto.profile.ProfileUpdateResponseDTO;
import com.example.iisdrugcrm.dto.profile.UserUpdateDTO;
import com.example.iisdrugcrm.exception.DuplicateUserException;
import com.example.iisdrugcrm.repository.RegionRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.security.JwtTokenProvider;
import com.example.iisdrugcrm.dto.team.TeamMemberDTO;
import com.example.iisdrugcrm.domain.UserRole;
import java.util.List;
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
    private final JwtTokenProvider tokenProvider;
    private final RegionRepository regionRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtTokenProvider tokenProvider,
            RegionRepository regionRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenProvider = tokenProvider;
        this.regionRepository = regionRepository;
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
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setActive(true);
        user.setHasChangedPassword(false);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        if (dto.getRole() == UserRole.ROLE_BUYER) {
            if (dto.getRegionId() != null) {
                user.setBuyerRegion(regionRepository.findById(dto.getRegionId())
                        .orElseThrow(() -> new IllegalArgumentException("Region not found")));
            }
            user.setCustomerSegment(dto.getCustomerSegment() == null ? null : dto.getCustomerSegment().trim());
        }

        User savedUser = userRepository.save(user);
        emailService.sendInitialCredentials(savedUser, dto.getPassword());
        return UserResponseDTO.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getProfile(String username) {
        return UserResponseDTO.fromEntity(getUser(username));
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return getUser(username).getId();
    }

    @Override
    public List<TeamMemberDTO> searchPricelistCreators(String username) {
        String query = username == null ? "" : username.trim();
        if (query.isBlank()) {
            return List.of();
        }

        return userRepository
            .findTop10ByUsernameContainingIgnoreCaseAndRoleAndIsActiveTrueOrderByUsernameAsc(
                query,
                UserRole.ROLE_PRICELIST_CREATOR
            )
            .stream()
            .map(TeamMemberDTO::fromEntity)
            .toList();
    }

    @Override
    @Transactional
    public ProfileUpdateResponseDTO updateProfile(String username, UserUpdateDTO dto) {
        User user = getUser(username);

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        User savedUser = userRepository.save(user);
        return buildProfileUpdateResponse(savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDTO changePassword(String username, PasswordChangeDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = getUser(username);
        user.changePassword(dto.getOldPassword(), dto.getNewPassword(), passwordEncoder);
        User savedUser = userRepository.save(user);
        return buildSessionResponse(savedUser);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private LoginResponseDTO buildSessionResponse(User user) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsername(user.getUsername());
        response.setRoles(List.of(user.getRole()));
        response.setActive(user.isActive());
        response.setHasChangedPassword(user.isHasChangedPassword());
        response.setToken(tokenProvider.generateToken(user.getUsername(), List.of(user.getRole()), user.isHasChangedPassword()));
        return response;
    }

    private ProfileUpdateResponseDTO buildProfileUpdateResponse(User user) {
        ProfileUpdateResponseDTO response = new ProfileUpdateResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setRoles(List.of(user.getRole()));
        response.setActive(user.isActive());
        response.setHasChangedPassword(user.isHasChangedPassword());
        response.setToken(tokenProvider.generateToken(user.getUsername(), List.of(user.getRole()), user.isHasChangedPassword()));
        return response;
    }
}
