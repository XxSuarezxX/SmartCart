package com.tienda.ecommerce.carrito.service;

import com.tienda.ecommerce.carrito.model.Carrito;
import com.tienda.ecommerce.carrito.repository.CarritoRepository;
import com.tienda.ecommerce.common.exception.RecursoNoEncontradoException;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Camiseta");
        producto.setPrecio(50.0);
    }

    @Test
    @DisplayName("obtenerCarrito() devuelve los items del usuario")
    void obtenerCarrito_devuelveItems() {
        Carrito item = new Carrito();
        item.setId(1L);
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(List.of(item));

        List<Carrito> resultado = carritoService.obtenerCarrito(1L);

        assertThat(resultado).hasSize(1).containsExactly(item);
    }

    @Test
    @DisplayName("agregarProducto() crea un nuevo item si el producto no estaba en el carrito")
    void agregarProducto_nuevoItem() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioIdAndProductoId(1L, 10L)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito resultado = carritoService.agregarProducto(1L, 10L, 3);

        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository).save(captor.capture());
        Carrito guardado = captor.getValue();

        assertThat(guardado.getUsuario()).isSameAs(usuario);
        assertThat(guardado.getProducto()).isSameAs(producto);
        assertThat(guardado.getCantidad()).isEqualTo(3);
        assertThat(resultado).isSameAs(guardado);
    }

    @Test
    @DisplayName("agregarProducto() suma la cantidad si el item ya existe en el carrito")
    void agregarProducto_itemExistente_sumaCantidad() {
        Carrito existente = new Carrito();
        existente.setId(7L);
        existente.setUsuario(usuario);
        existente.setProducto(producto);
        existente.setCantidad(2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioIdAndProductoId(1L, 10L)).thenReturn(Optional.of(existente));
        when(carritoRepository.save(existente)).thenReturn(existente);

        Carrito resultado = carritoService.agregarProducto(1L, 10L, 4);

        assertThat(resultado.getCantidad()).isEqualTo(6);
        verify(carritoRepository).save(existente);
    }

    @Test
    @DisplayName("agregarProducto() lanza excepción cuando el usuario no existe")
    void agregarProducto_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarProducto(99L, 10L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("agregarProducto() lanza excepción cuando el producto no existe")
    void agregarProducto_productoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarProducto(1L, 99L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarCantidad() modifica la cantidad y guarda")
    void actualizarCantidad_modificaYGuarda() {
        Carrito item = new Carrito();
        item.setId(3L);
        item.setCantidad(1);
        when(carritoRepository.findById(3L)).thenReturn(Optional.of(item));
        when(carritoRepository.save(item)).thenReturn(item);

        Carrito resultado = carritoService.actualizarCantidad(3L, 8);

        assertThat(resultado.getCantidad()).isEqualTo(8);
        verify(carritoRepository).save(item);
    }

    @Test
    @DisplayName("actualizarCantidad() lanza excepción cuando el item no existe")
    void actualizarCantidad_itemNoExiste_lanzaExcepcion() {
        when(carritoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.actualizarCantidad(404L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Item no encontrado en el carrito");
    }

    @Test
    @DisplayName("eliminarProducto() invoca deleteById en el repositorio")
    void eliminarProducto_invocaDeleteById() {
        carritoService.eliminarProducto(2L);

        verify(carritoRepository).deleteById(2L);
    }

    @Test
    @DisplayName("vaciarCarrito() invoca deleteByUsuarioId")
    void vaciarCarrito_invocaDeleteByUsuarioId() {
        carritoService.vaciarCarrito(1L);

        verify(carritoRepository).deleteByUsuarioId(1L);
    }
}
