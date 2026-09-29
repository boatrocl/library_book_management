package com.libraflow.library.service;

import com.libraflow.library.dto.request.LoginRequest;
import com.libraflow.library.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(
            LoginRequest request
    );
}