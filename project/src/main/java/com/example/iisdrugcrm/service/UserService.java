package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.UserCreateDTO;
import com.example.iisdrugcrm.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDTO create(UserCreateDTO dto);

    Page<UserResponseDTO> getAll(Pageable pageable);
}