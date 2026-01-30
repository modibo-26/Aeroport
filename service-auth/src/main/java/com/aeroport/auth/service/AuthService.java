package com.aeroport.auth.service;

import com.aeroport.auth.dto.AuthResponse;
import com.aeroport.auth.dto.LoginRequest;
import com.aeroport.auth.dto.RegisterRequest;
import com.aeroport.auth.entity.Role;
import com.aeroport.auth.entity.User;
import com.aeroport.auth.repository.UserRepository;
import com.aeroport.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final JwtUtil jwt;
    private final PasswordEncoder encoder;

    public AuthResponse register(RegisterRequest request) {
        if(repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .role(Role.PASSAGER)
                .build();

        user = repository.save(user);

        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(null, user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        String token = jwt.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}
