package com.libraflow.library.service;

import com.libraflow.library.dto.request.LoginRequest;
import com.libraflow.library.dto.response.AuthResponse;
import com.libraflow.library.dto.request.RegisterRequest;

public interface AuthService {

    AuthResponse login(
            LoginRequest request
    );

    AuthResponse register(
        RegisterRequest request
    );
}