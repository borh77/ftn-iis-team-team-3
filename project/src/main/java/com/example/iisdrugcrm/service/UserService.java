package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.UserCreateDTO;
import com.example.iisdrugcrm.dto.UserResponseDTO;
import com.example.iisdrugcrm.dto.auth.LoginResponseDTO;
import com.example.iisdrugcrm.dto.profile.PasswordChangeDTO;
import com.example.iisdrugcrm.dto.profile.ProfileUpdateResponseDTO;
import com.example.iisdrugcrm.dto.profile.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDTO create(UserCreateDTO dto);

    Page<UserResponseDTO> getAll(Pageable pageable);

    UserResponseDTO getProfile(String username);

    ProfileUpdateResponseDTO updateProfile(String username, UserUpdateDTO dto);

    LoginResponseDTO changePassword(String username, PasswordChangeDTO dto);
}