package com.tienda.ecommerce.usuarios.service;

import com.tienda.ecommerce.usuarios.dto.LoginResponse;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
import com.tienda.ecommerce.usuarios.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsuarioService {

    public java.util.List<Usuario> listarTodos() {
    return usuarioRepository.findAll();
}

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // Añadimos la utilidad del token

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Usuario registrar(Usuario usuario) {
    String passwordHasheada = passwordEncoder.encode(usuario.getPassword());
    usuario.setPassword(passwordHasheada);
    
    if (usuario.getRol() == null) {
        usuario.setRol(Usuario.Rol.CLIENTE); // ← rol por defecto
    }
    
    return usuarioRepository.save(usuario);
}

    // Método para el Login
    public LoginResponse autenticar(String email, String password) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

    if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        if (passwordEncoder.matches(password, usuario.getPassword())) {
            String token = jwtUtil.generarToken(usuario);
            return new LoginResponse(usuario.getId(), token, usuario.getNombre(), usuario.getRol().name());
        }
    }

    throw new RuntimeException("Credenciales incorrectas");
}


}