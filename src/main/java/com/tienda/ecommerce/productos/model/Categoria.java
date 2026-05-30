package com.tienda.ecommerce.productos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categorias")
@Getter @Setter
public class Categoria {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;
}