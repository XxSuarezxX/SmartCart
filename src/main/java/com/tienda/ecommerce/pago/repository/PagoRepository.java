package com.tienda.ecommerce.pago.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tienda.ecommerce.pago.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("SELECT DISTINCT p FROM Pago p LEFT JOIN FETCH p.items WHERE p.usuario.id = :usuarioId ORDER BY p.fechaPago DESC")
    List<Pago> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}