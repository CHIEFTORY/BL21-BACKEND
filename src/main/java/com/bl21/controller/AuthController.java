package com.bl21.controller;

import com.bl21.dto.request.LoginRequest;
import com.bl21.dto.request.RegisterRequest;
import com.bl21.dto.response.AuthResponse;
import com.bl21.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {

        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return new AuthResponse(
                "User registered successfully"
        );
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {

        String token = authService.login(request);

        return new AuthResponse(token);
    }
}