package com.tienda.ecommerce.pago.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pago_items")
@Getter
@Setter
public class PagoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pago_id", nullable = false)
    @JsonIgnore  // ← esto corta la referencia circular
    private Pago pago;

    private String nombreProducto;
    private Double precioUnitario;
    private Integer cantidad;
    private String urlImagen;
}