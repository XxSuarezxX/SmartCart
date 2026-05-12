package com.tienda.ecommerce.usuarios.controller;

import com.tienda.ecommerce.usuarios.dto.LoginRequest;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
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
    public String login(@RequestBody LoginRequest loginRequest) {
        // Devuelve el token si las credenciales son correctas
        return usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getPassword());
    }
}