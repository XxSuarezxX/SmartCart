package com.tienda.ecommerce.common.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio
 * (por ejemplo, pagar con el carrito vacío o un CSV inválido).
 * El {@link GlobalExceptionHandler} la traduce a un HTTP 400.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}