package com.tienda.ecommerce.pago.service;

import com.tienda.ecommerce.carrito.model.Carrito;
import com.tienda.ecommerce.carrito.service.CarritoService;
import com.tienda.ecommerce.common.exception.RecursoNoEncontradoException;
import com.tienda.ecommerce.common.exception.ReglaNegocioException;
import com.tienda.ecommerce.pago.dto.PagoRequest;
import com.tienda.ecommerce.pago.model.EstadoPago;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.repository.PagoRepository;
import com.tienda.ecommerce.productos.model.Categoria;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.model.TipoInteraccion;
import com.tienda.ecommerce.recomendacion.service.RecomendacionService;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private CarritoService carritoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecomendacionService recomendacionService;

    @Mock
    private ReciboPdfService reciboPdfService;

    @Mock
    private EmailService emailService;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PagoService pagoService;

    private Usuario usuario;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");

        categoria = new Categoria();
        categoria.setId(50L);
        categoria.setNombre("Oversize");
    }

    private Carrito itemCarrito(Long productoId, double precio, int cantidad) {
        Producto p = new Producto();
        p.setId(productoId);
        p.setPrecio(precio);
        p.setCategoria(categoria);

        Carrito c = new Carrito();
        c.setProducto(p);
        c.setUsuario(usuario);
        c.setCantidad(cantidad);
        return c;
    }

    @Test
    @DisplayName("procesarPago() calcula el total, persiste el pago, guarda interacciones y vacía el carrito")
    void procesarPago_exitoso() {
        PagoRequest request = new PagoRequest();
        request.setUsuarioId(1L);

        Carrito item1 = itemCarrito(10L, 50.0, 2); // 100
        Carrito item2 = itemCarrito(11L, 30.0, 3); // 90

        when(carritoService.obtenerCarrito(1L)).thenReturn(List.of(item1, item2));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reciboPdfService.generarRecibo(any(Pago.class), anyList())).thenReturn(new byte[]{1, 2, 3});

        Pago resultado = pagoService.procesarPago(request);

        assertThat(resultado.getMontoTotal()).isEqualTo(190.0);
        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.EXITOSO);
        assertThat(resultado.getUsuario()).isSameAs(usuario);
        assertThat(resultado.getFechaPago()).isNotNull();

        ArgumentCaptor<Interaccion> interaccionCaptor = ArgumentCaptor.forClass(Interaccion.class);
        verify(recomendacionService, times(2)).guardarInteraccion(interaccionCaptor.capture());
        List<Interaccion> interacciones = interaccionCaptor.getAllValues();
        assertThat(interacciones).allSatisfy(i -> {
            assertThat(i.getUsuarioId()).isEqualTo(1L);
            assertThat(i.getCategoriaId()).isEqualTo(50L);
            assertThat(i.getTipo()).isEqualTo(TipoInteraccion.COMPRA);
        });
        assertThat(interacciones).extracting(Interaccion::getProductoId).containsExactly(10L, 11L);

        verify(carritoService).vaciarCarrito(1L);
        verify(pagoRepository).save(any(Pago.class));

        verify(reciboPdfService).generarRecibo(any(Pago.class), anyList());
        verify(emailService).enviarReciboCompra(eq("juan@test.com"), eq("Juan"), any(), any(byte[].class));
    }

    @Test
    @DisplayName("procesarPago() lanza excepción cuando el carrito está vacío")
    void procesarPago_carritoVacio_lanzaExcepcion() {
        PagoRequest request = new PagoRequest();
        request.setUsuarioId(1L);
        when(carritoService.obtenerCarrito(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> pagoService.procesarPago(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El carrito está vacío");

        verify(pagoRepository, never()).save(any());
        verify(carritoService, never()).vaciarCarrito(any());
        verify(recomendacionService, never()).guardarInteraccion(any());
    }

    @Test
    @DisplayName("procesarPago() lanza excepción cuando el usuario no existe")
    void procesarPago_usuarioNoExiste_lanzaExcepcion() {
        PagoRequest request = new PagoRequest();
        request.setUsuarioId(99L);
        when(carritoService.obtenerCarrito(99L)).thenReturn(List.of(itemCarrito(10L, 50.0, 1)));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.procesarPago(request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");

        verify(pagoRepository, never()).save(any());
    }
}
