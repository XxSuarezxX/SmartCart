package com.tienda.ecommerce.recomendacion.model;

/**
 * Tipos de interacción que un usuario puede tener con un producto.
 * Cada tipo lleva asociado su peso ("puntos") para el cálculo de
 * recomendaciones, de modo que añadir un nuevo tipo no obliga a tocar
 * la lógica de negocio (principio Abierto/Cerrado).
 */
public enum TipoInteraccion {

    LIKE(1),
    CARRITO(2),
    COMPRA(3);

    private final int puntos;

    TipoInteraccion(int puntos) {
        this.puntos = puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    /**
     * Convierte el texto recibido en la API (p. ej. "like") al tipo correspondiente.
     * Tolera mayúsculas/minúsculas y espacios. Devuelve {@code null} si el valor
     * es nulo, vacío o no corresponde a ningún tipo conocido.
     */
    public static TipoInteraccion fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return TipoInteraccion.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}