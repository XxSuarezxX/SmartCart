package com.tienda.ecommerce.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private long id;
    private String token;
    private String username;
    private String rol;
}