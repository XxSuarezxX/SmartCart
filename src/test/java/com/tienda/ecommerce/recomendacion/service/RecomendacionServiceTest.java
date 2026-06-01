package com.tienda.ecommerce.recomendacion.service;

import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.repository.InteraccionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecomendacionServiceTest {

    @Mock
    private InteraccionRepository interaccionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private RecomendacionService recomendacionService;

    @ParameterizedTest(name = "tipo \"{0}\" => {1} puntos")
    @CsvSource({
            "LIKE,1",
            "CARRITO,2",
            "COMPRA,3",
            "OTRO,0"
    })
    @DisplayName("guardarInteraccion() asigna los puntos segun el tipo")
    void guardarInteraccion_asignaPuntosSegunTipo(String tipo, int puntosEsperados) {
        Interaccion interaccion = new Interaccion();
        interaccion.setTipo(tipo);
        when(interaccionRepository.save(any(Interaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Interaccion resultado = recomendacionService.guardarInteraccion(interaccion);

        ArgumentCaptor<Interaccion> captor = ArgumentCaptor.forClass(Interaccion.class);
        verify(interaccionRepository).save(captor.capture());
        assertThat(captor.getValue().getPuntos()).isEqualTo(puntosEsperados);
        assertThat(resultado.getPuntos()).isEqualTo(puntosEsperados);
    }

    @ParameterizedTest(name = "tipo \"{0}\" en minusculas también suma puntos")
    @CsvSource({
            "like,1",
            "carrito,2",
            "compra,3"
    })
    @DisplayName("guardarInteraccion() normaliza el tipo a mayusculas")
    void guardarInteraccion_normalizaTipo(String tipo, int puntosEsperados) {
        Interaccion interaccion = new Interaccion();
        interaccion.setTipo(tipo);
        when(interaccionRepository.save(any(Interaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Interaccion resultado = recomendacionService.guardarInteraccion(interaccion);

        assertThat(resultado.getPuntos()).isEqualTo(puntosEsperados);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("guardarInteraccion() asigna 0 puntos cuando el tipo es null")
    void guardarInteraccion_tipoNull_asignaCeroPuntos(String tipo) {
        Interaccion interaccion = new Interaccion();
        interaccion.setTipo(tipo);
        when(interaccionRepository.save(any(Interaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Interaccion resultado = recomendacionService.guardarInteraccion(interaccion);

        assertThat(resultado.getPuntos()).isZero();
    }

    @Test
    @DisplayName("obtenerRecomendaciones() devuelve los productos de la categoria favorita")
    void obtenerRecomendaciones_conPreferencias_devuelveCategoriaFavorita() {
        // findPreferenciasPorUsuario ya viene ordenado DESC: la categoria 7 es la favorita
        List<Object[]> preferencias = List.of(
                new Object[]{7L, 10L},
                new Object[]{3L, 4L}
        );
        Producto p1 = new Producto();
        p1.setId(100L);
        Producto p2 = new Producto();
        p2.setId(101L);
        when(interaccionRepository.findPreferenciasPorUsuario(1L)).thenReturn(preferencias);
        when(productoRepository.findByCategoriaId(7L)).thenReturn(List.of(p1, p2));

        List<Producto> resultado = recomendacionService.obtenerRecomendaciones(1L);

        assertThat(resultado).containsExactly(p1, p2);
        verify(productoRepository).findByCategoriaId(7L);
    }

    @Test
    @DisplayName("obtenerRecomendaciones() devuelve hasta 5 productos cuando el usuario no tiene interacciones")
    void obtenerRecomendaciones_sinPreferencias_devuelvePrimeros5() {
        when(interaccionRepository.findPreferenciasPorUsuario(1L)).thenReturn(List.of());
        List<Producto> catalogo = IntStream.rangeClosed(1, 8)
                .mapToObj(i -> {
                    Producto p = new Producto();
                    p.setId((long) i);
                    return p;
                })
                .toList();
        when(productoRepository.findAll()).thenReturn(catalogo);

        List<Producto> resultado = recomendacionService.obtenerRecomendaciones(1L);

        assertThat(resultado).hasSize(5);
        assertThat(resultado).containsExactlyElementsOf(catalogo.subList(0, 5));
    }

    @Test
    @DisplayName("obtenerRankingGlobal() delega en el repositorio")
    void obtenerRankingGlobal_delegaEnRepositorio() {
        List<Object[]> ranking = List.<Object[]>of(new Object[]{5L, 42L});
        when(interaccionRepository.findRankingCategoriasGlobal()).thenReturn(ranking);

        List<Object[]> resultado = recomendacionService.obtenerRankingGlobal();

        assertThat(resultado).isSameAs(ranking);
        verify(interaccionRepository).findRankingCategoriasGlobal();
    }
}
