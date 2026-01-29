package com.aeroport.auth.controller;

import com.aeroport.auth.dto.AuthResponse;
import com.aeroport.auth.dto.LoginRequest;
import com.aeroport.auth.dto.RegisterRequest;
import com.aeroport.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request)  {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return service.login(request);
    }
}
