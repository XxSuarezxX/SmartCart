package com.tienda.ecommerce.pago.model;

/**
 * Estados posibles de un pago. Se persiste como texto
 * ({@code @Enumerated(EnumType.STRING)}) para mantener legible la base de datos
 * y compatible con los registros existentes ("EXITOSO", "RECHAZADO").
 */
public enum EstadoPago {
    EXITOSO,
    RECHAZADO
}