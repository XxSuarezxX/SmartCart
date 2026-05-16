package com.tienda.ecommerce.usuarios.controller;

import com.tienda.ecommerce.usuarios.dto.LoginRequest;
import com.tienda.ecommerce.usuarios.dto.LoginResponse;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public Usuario registrar(@RequestBody Usuario usuario) {
        return usuarioService.registrar(usuario);
    }

    @PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    try {
        LoginResponse response = usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
}