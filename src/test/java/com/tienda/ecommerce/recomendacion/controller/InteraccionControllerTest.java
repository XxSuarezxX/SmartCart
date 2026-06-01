package com.tienda.ecommerce.recomendacion.controller;

import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.recomendacion.dto.InteraccionDTO;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.model.TipoInteraccion;
import com.tienda.ecommerce.recomendacion.service.RecomendacionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteraccionControllerTest {

    @Mock
    private RecomendacionService recomendacionService;

    @InjectMocks
    private InteraccionController interaccionController;

    @Test
    @DisplayName("registrar() mapea solo usuarioId, categoriaId y tipo del DTO a la entidad")
    void registrar_mapeaSoloCamposPermitidos() {
        InteraccionDTO dto = new InteraccionDTO();
        dto.setUsuarioId(1L);
        dto.setCategoriaId(7L);
        dto.setTipo("LIKE");
        when(recomendacionService.guardarInteraccion(any(Interaccion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        interaccionController.registrar(dto);

        ArgumentCaptor<Interaccion> captor = ArgumentCaptor.forClass(Interaccion.class);
        verify(recomendacionService).guardarInteraccion(captor.capture());
        Interaccion enviada = captor.getValue();

        assertThat(enviada.getUsuarioId()).isEqualTo(1L);
        assertThat(enviada.getCategoriaId()).isEqualTo(7L);
        assertThat(enviada.getTipo()).isEqualTo(TipoInteraccion.LIKE);
        // El id y los puntos no se inyectan desde el DTO: los calcula/asigna el servicio.
        assertThat(enviada.getId()).isNull();
        assertThat(enviada.getPuntos()).isZero();
    }

    @Test
    @DisplayName("registrar() devuelve la interaccion que retorna el servicio")
    void registrar_devuelveResultadoDelServicio() {
        InteraccionDTO dto = new InteraccionDTO();
        dto.setTipo("COMPRA");
        Interaccion guardada = new Interaccion();
        guardada.setId(99L);
        when(recomendacionService.guardarInteraccion(any(Interaccion.class))).thenReturn(guardada);

        Interaccion resultado = interaccionController.registrar(dto);

        assertThat(resultado).isSameAs(guardada);
    }

    @Test
    @DisplayName("obtenerSugeridos() delega en el servicio con el usuarioId recibido")
    void obtenerSugeridos_delegaEnServicio() {
        Producto producto = new Producto();
        producto.setId(10L);
        when(recomendacionService.obtenerRecomendaciones(1L)).thenReturn(List.of(producto));

        List<Producto> resultado = interaccionController.obtenerSugeridos(1L);

        assertThat(resultado).containsExactly(producto);
        verify(recomendacionService).obtenerRecomendaciones(1L);
    }

    @Test
    @DisplayName("obtenerRanking() devuelve el ranking global del servicio")
    void obtenerRanking_devuelveRankingGlobal() {
        List<Object[]> ranking = List.<Object[]>of(new Object[]{5L, 42L});
        when(recomendacionService.obtenerRankingGlobal()).thenReturn(ranking);

        List<Object[]> resultado = interaccionController.obtenerRanking();

        assertThat(resultado).isSameAs(ranking);
    }
}
