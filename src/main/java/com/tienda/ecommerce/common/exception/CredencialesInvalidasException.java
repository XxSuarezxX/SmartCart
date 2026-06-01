package com.tienda.ecommerce.common.exception;

/**
 * Se lanza cuando las credenciales de autenticación no son válidas.
 * El {@link GlobalExceptionHandler} la traduce a un HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}