package com.tienda.ecommerce.productos.service;

import com.tienda.ecommerce.productos.model.Categoria;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.CategoriaRepository;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("listarTodos() devuelve la lista del repositorio")
    void listarTodos_devuelveProductos() {
        Producto p1 = new Producto();
        p1.setId(1L);
        Producto p2 = new Producto();
        p2.setId(2L);
        when(productoRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Producto> resultado = productoService.listarTodos();

        assertThat(resultado).hasSize(2).containsExactly(p1, p2);
    }

    @Test
    @DisplayName("findById() devuelve el producto cuando existe")
    void findById_existe_devuelveProducto() {
        Producto p = new Producto();
        p.setId(7L);
        when(productoRepository.findById(7L)).thenReturn(Optional.of(p));

        Producto resultado = productoService.findById(7L);

        assertThat(resultado).isSameAs(p);
    }

    @Test
    @DisplayName("findById() devuelve null cuando el producto no existe")
    void findById_noExiste_devuelveNull() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Producto resultado = productoService.findById(99L);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("guardarProducto() delega en el repositorio")
    void guardarProducto_delegaEnRepositorio() {
        Producto p = new Producto();
        p.setNombre("Camiseta");
        when(productoRepository.save(p)).thenReturn(p);

        Producto guardado = productoService.guardarProducto(p);

        assertThat(guardado).isSameAs(p);
        verify(productoRepository).save(p);
    }

    @Test
    @DisplayName("eliminarProducto() invoca deleteById en el repositorio")
    void eliminarProducto_invocaDeleteById() {
        productoService.eliminarProducto(5L);

        verify(productoRepository).deleteById(5L);
    }

    @Test
    @DisplayName("guardarCategoria() persiste y devuelve la categoría")
    void guardarCategoria_persiste() {
        Categoria c = new Categoria();
        c.setNombre("Oversize");
        when(categoriaRepository.save(c)).thenReturn(c);

        Categoria guardada = productoService.guardarCategoria(c);

        assertThat(guardada.getNombre()).isEqualTo("Oversize");
        verify(categoriaRepository).save(c);
    }

    @Test
    @DisplayName("listarCategorias() devuelve todas las categorías")
    void listarCategorias_devuelveCategorias() {
        Categoria c = new Categoria();
        c.setNombre("New Era");
        when(categoriaRepository.findAll()).thenReturn(List.of(c));

        List<Categoria> resultado = productoService.listarCategorias();

        assertThat(resultado).hasSize(1).containsExactly(c);
    }
}
