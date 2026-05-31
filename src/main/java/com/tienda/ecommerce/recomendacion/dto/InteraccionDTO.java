package com.tienda.ecommerce.recomendacion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InteraccionDTO {
    private Long usuarioId;
    private Long categoriaId;
    private String tipo;
}