package com.tienda.ecommerce.carrito.service;

import com.tienda.ecommerce.carrito.model.Carrito;
import com.tienda.ecommerce.carrito.repository.CarritoRepository;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<Carrito> obtenerCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    public Carrito agregarProducto(Long usuarioId, Long productoId, int cantidad) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Si el producto ya está en el carrito, suma la cantidad
        return carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId)
                .map(item -> {
                    item.setCantidad(item.getCantidad() + cantidad);
                    return carritoRepository.save(item);
                })
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(cantidad);
                    return carritoRepository.save(nuevo);
                });
    }

    public Carrito actualizarCantidad(Long carritoId, int cantidad) {
        Carrito item = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en el carrito"));
        item.setCantidad(cantidad);
        return carritoRepository.save(item);
    }

    public void eliminarProducto(Long carritoId) {
        carritoRepository.deleteById(carritoId);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        carritoRepository.deleteByUsuarioId(usuarioId);
    }
}
