package com.tienda.ecommerce.usuarios.repository;

import com.tienda.ecommerce.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Lo usaremos para el login y para evitar correos duplicados
    Optional<Usuario> findByEmail(String email);
}