package com.tienda.ecommerce.productos.csv;

/**
 * Una fila ya tokenizada y con tipos convertidos del CSV de productos.
 * No conoce nada de persistencia: solo transporta los datos parseados.
 */
public record FilaProductoCsv(
        int numeroLinea,
        String descripcion,
        String nombre,
        Double precio,
        int stock,
        String urlImagen,
        Long categoriaId,
        String colores,
        String tallas) {
}