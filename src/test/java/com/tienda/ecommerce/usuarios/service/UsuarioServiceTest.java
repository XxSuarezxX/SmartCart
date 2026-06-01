package com.tienda.ecommerce.usuarios.service;

import com.tienda.ecommerce.common.exception.CredencialesInvalidasException;
import com.tienda.ecommerce.usuarios.dto.LoginResponse;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
import com.tienda.ecommerce.usuarios.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        usuario.setPassword("clave123");
        usuario.setRol(Usuario.Rol.CLIENTE);
    }

    @Test
    @DisplayName("registrar() debe hashear la contraseña y persistir el usuario")
    void registrar_hasheaPasswordYGuarda() {
        when(passwordEncoder.encode("clave123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.registrar(usuario);

        assertThat(guardado.getPassword()).isEqualTo("HASH");
        assertThat(guardado.getRol()).isEqualTo(Usuario.Rol.CLIENTE);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("registrar() asigna rol CLIENTE por defecto cuando viene nulo")
    void registrar_asignaRolPorDefecto() {
        usuario.setRol(null);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.registrar(usuario);

        assertThat(guardado.getRol()).isEqualTo(Usuario.Rol.CLIENTE);
    }

    @Test
    @DisplayName("autenticar() devuelve LoginResponse con token cuando las credenciales son válidas")
    void autenticar_credencialesValidas_devuelveLoginResponse() {
        usuario.setPassword("HASH");
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "HASH")).thenReturn(true);
        when(jwtUtil.generarToken(usuario)).thenReturn("token-jwt");

        LoginResponse response = usuarioService.autenticar("juan@test.com", "clave123");

        assertThat(response.getToken()).isEqualTo("token-jwt");
        assertThat(response.getUsername()).isEqualTo("Juan");
        assertThat(response.getRol()).isEqualTo("CLIENTE");
    }

    @Test
    @DisplayName("autenticar() lanza excepción cuando el email no existe")
    void autenticar_emailNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.autenticar("nadie@test.com", "x"))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales incorrectas");

        verify(jwtUtil, never()).generarToken(any());
    }

    @Test
    @DisplayName("autenticar() lanza excepción cuando la contraseña no coincide")
    void autenticar_passwordIncorrecta_lanzaExcepcion() {
        usuario.setPassword("HASH");
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.autenticar("juan@test.com", "mala"))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales incorrectas");

        verify(jwtUtil, never()).generarToken(any());
    }
}
