package com.tienda.ecommerce.usuarios.controller;

import com.tienda.ecommerce.usuarios.dto.LoginRequest;
import com.tienda.ecommerce.usuarios.dto.LoginResponse;
import com.tienda.ecommerce.usuarios.dto.UsuarioResponse;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarTodos().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @PostMapping("/registro")
    public UsuarioResponse registrar(@RequestBody Usuario usuario) {
        return UsuarioResponse.from(usuarioService.registrar(usuario));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        // Si las credenciales son inválidas, el servicio lanza
        // CredencialesInvalidasException y el GlobalExceptionHandler responde 401.
        return usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getPassword());
    }
}