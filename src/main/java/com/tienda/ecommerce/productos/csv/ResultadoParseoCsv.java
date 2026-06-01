package com.tienda.ecommerce.productos.csv;

import java.util.List;

/**
 * Resultado de parsear un CSV de productos: las filas válidas y los
 * mensajes de error de las líneas que no se pudieron interpretar.
 */
public record ResultadoParseoCsv(List<FilaProductoCsv> filas, List<String> errores) {
}