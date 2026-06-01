package com.tienda.ecommerce.usuarios.dto;

import com.tienda.ecommerce.usuarios.model.Usuario;

import lombok.Getter;

/**
 * Vista pública de un usuario para las respuestas de la API.
 * Deliberadamente NO expone la contraseña (ni su hash).
 */
@Getter
public class UsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String email;
    private final String rol;

    private UsuarioResponse(Long id, String nombre, String email, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol() != null ? usuario.getRol().name() : null);
    }
}