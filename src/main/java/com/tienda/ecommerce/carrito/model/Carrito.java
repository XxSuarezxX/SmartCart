package com.tienda.ecommerce.carrito.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.productos.model.Producto;

@Entity
@Table(name = "carrito")
@Getter
@Setter
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;
}
