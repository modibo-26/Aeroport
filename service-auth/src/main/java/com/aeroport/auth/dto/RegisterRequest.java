package com.aeroport.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String nom;
    private String prenom;
}
