package com.tienda.ecommerce.usuarios.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    // El cliente puede enviar la contraseña (registro/login) pero nunca se
    // serializa de vuelta en las respuestas JSON, ni siquiera de forma transitiva
    // (p. ej. dentro de un Pago que referencia al Usuario).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    public enum Rol {
        ADMIN, CLIENTE
    }
}