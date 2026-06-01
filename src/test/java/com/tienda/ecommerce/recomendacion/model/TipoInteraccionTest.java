package com.tienda.ecommerce.recomendacion.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TipoInteraccionTest {

    @ParameterizedTest(name = "\"{0}\" => {1}")
    @CsvSource({
            "LIKE,LIKE",
            "like,LIKE",
            "  Carrito  ,CARRITO",
            "COMPRA,COMPRA"
    })
    @DisplayName("fromString() normaliza mayúsculas/minúsculas y espacios")
    void fromString_normaliza(String entrada, TipoInteraccion esperado) {
        assertThat(TipoInteraccion.fromString(entrada)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "DESCONOCIDO", "xyz"})
    @DisplayName("fromString() devuelve null para valores nulos, vacíos o desconocidos")
    void fromString_valoresInvalidos_devuelveNull(String entrada) {
        assertThat(TipoInteraccion.fromString(entrada)).isNull();
    }

    @Test
    @DisplayName("los puntos reflejan el peso de cada tipo")
    void puntos_porTipo() {
        assertThat(TipoInteraccion.LIKE.getPuntos()).isEqualTo(1);
        assertThat(TipoInteraccion.CARRITO.getPuntos()).isEqualTo(2);
        assertThat(TipoInteraccion.COMPRA.getPuntos()).isEqualTo(3);
    }
}