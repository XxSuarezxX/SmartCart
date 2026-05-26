package com.tienda.ecommerce.usuarios.util;

import com.tienda.ecommerce.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    // 32+ bytes para HS256
    private static final String SECRET = "secreto-de-pruebas-muy-largo-para-hs256-1234567890";

    private JwtUtil jwtUtil;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        usuario.setRol(Usuario.Rol.ADMIN);
    }

    @Test
    @DisplayName("generarToken() devuelve un JWT no vacío")
    void generarToken_devuelveTokenNoVacio() {
        String token = jwtUtil.generarToken(usuario);

        assertThat(token).isNotBlank();
        // Estructura header.payload.signature
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extraerUsername() devuelve el email contenido en el token")
    void extraerUsername_devuelveEmail() {
        String token = jwtUtil.generarToken(usuario);

        String username = jwtUtil.extraerUsername(token);

        assertThat(username).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("extraerRol() devuelve el rol contenido en el token")
    void extraerRol_devuelveRol() {
        String token = jwtUtil.generarToken(usuario);

        String rol = jwtUtil.extraerRol(token);

        assertThat(rol).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("esTokenValido() devuelve true para un token firmado correctamente")
    void esTokenValido_tokenValido() {
        String token = jwtUtil.generarToken(usuario);

        assertThat(jwtUtil.esTokenValido(token)).isTrue();
    }

    @Test
    @DisplayName("esTokenValido() devuelve false para un token corrupto")
    void esTokenValido_tokenCorrupto() {
        assertThat(jwtUtil.esTokenValido("token.invalido.xxx")).isFalse();
    }

    @Test
    @DisplayName("esTokenValido() devuelve false si el token fue firmado con otra clave")
    void esTokenValido_firmaDistinta() {
        String token = jwtUtil.generarToken(usuario);

        JwtUtil otro = new JwtUtil();
        ReflectionTestUtils.setField(otro, "secretKey",
                "otra-clave-totalmente-distinta-pero-igual-de-larga-1234567890");

        assertThat(otro.esTokenValido(token)).isFalse();
    }
}
