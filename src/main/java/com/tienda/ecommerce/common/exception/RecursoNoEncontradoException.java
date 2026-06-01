package com.tienda.ecommerce.common.exception;

/**
 * Se lanza cuando se solicita una entidad que no existe.
 * El {@link GlobalExceptionHandler} la traduce a un HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}