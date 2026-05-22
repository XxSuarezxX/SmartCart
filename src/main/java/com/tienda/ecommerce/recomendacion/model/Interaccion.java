package com.tienda.ecommerce.recomendacion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Interaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long usuarioId;
    private Long productoId;
    private String tipo; // "LIKE", "CARRITO", "COMPRA"
    private int puntos;
    private LocalDateTime fecha = LocalDateTime.now();
    private Long categoriaId;
}