package com.tienda.ecommerce.carrito.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarritoRequest {
    private Long productoId;
    private int cantidad;
}
