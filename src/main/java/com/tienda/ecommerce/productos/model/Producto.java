package com.tienda.ecommerce.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter @Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    private Integer stock;

    private String urlImagen;

    @ManyToOne // Muchos productos pertenecen a una categoría
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}