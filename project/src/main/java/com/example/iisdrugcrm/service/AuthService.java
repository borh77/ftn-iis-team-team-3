package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.auth.LoginRequestDTO;
import com.example.iisdrugcrm.dto.auth.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    void logout(String authorizationHeader);
}