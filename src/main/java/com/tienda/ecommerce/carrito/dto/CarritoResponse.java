package com.tienda.ecommerce.carrito.dto;

import com.tienda.ecommerce.carrito.model.Carrito;

public record CarritoResponse(
        Long id,
        ProductoResumen producto,
        int cantidad) {

    public static CarritoResponse from(Carrito carrito) {
        var p = carrito.getProducto();
        var c = p.getCategoria();
        CategoriaResumen categoria = c == null ? null : new CategoriaResumen(c.getId(), c.getNombre());
        return new CarritoResponse(
                carrito.getId(),
                new ProductoResumen(p.getId(), p.getNombre(), p.getPrecio(), p.getUrlImagen(), p.getStock(), categoria),
                carrito.getCantidad());
    }

    public record ProductoResumen(Long id, String nombre, Double precio, String urlImagen, Integer stock,
            CategoriaResumen categoria) {
    }

    public record CategoriaResumen(Long id, String nombre) {
    }
}