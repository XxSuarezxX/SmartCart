package com.tienda.ecommerce.pago.service;

import com.tienda.ecommerce.carrito.model.Carrito;
import com.tienda.ecommerce.carrito.service.CarritoService;
import com.tienda.ecommerce.pago.dto.PagoRequest;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.repository.PagoRepository;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.service.RecomendacionService;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;
    private final RecomendacionService recomendacionService;

    public PagoService(PagoRepository pagoRepository, CarritoService carritoService,
                       UsuarioRepository usuarioRepository, RecomendacionService recomendacionService) {
        this.pagoRepository = pagoRepository;
        this.carritoService = carritoService;
        this.usuarioRepository = usuarioRepository;
        this.recomendacionService = recomendacionService;
    }

    @Transactional
    public Pago procesarPago(PagoRequest request) {
        // 1. Buscar los items del carrito del usuario
        List<Carrito> items = carritoService.obtenerCarrito(request.getUsuarioId());
        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 2. Calcular el total acumulado
        double total = items.stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();

        // 3. Crear el registro del Pago
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Pago pago = new Pago();
        pago.setUsuario(usuario);
        pago.setMontoTotal(total);
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado("EXITOSO");

        for (Carrito item : items) {
            Interaccion nuevaInteraccion = new Interaccion();
            nuevaInteraccion.setUsuarioId(usuario.getId());
            nuevaInteraccion.setProductoId(item.getProducto().getId());

            // Obtenemos el ID de la categoría desde el objeto Producto del carrito
            nuevaInteraccion.setCategoriaId(item.getProducto().getCategoria().getId());
            nuevaInteraccion.setTipo("COMPRA");
            // La fecha ya se inicializa sola con LocalDateTime.now() en tu modelo

            recomendacionService.guardarInteraccion(nuevaInteraccion);
        }

        // 5. Vaciar el carrito y guardar el pago
        carritoService.vaciarCarrito(usuario.getId());
        return pagoRepository.save(pago);
    }
}