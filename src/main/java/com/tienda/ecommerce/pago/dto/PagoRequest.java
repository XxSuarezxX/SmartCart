package com.tienda.ecommerce.pago.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoRequest {
    private Long usuarioId;
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaVencimiento;
    private String cvv;
    private String direccionEnvio;
}