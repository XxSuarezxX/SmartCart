package com.tienda.ecommerce.pago.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tienda.ecommerce.carrito.model.Carrito;
import com.tienda.ecommerce.carrito.service.CarritoService;
import com.tienda.ecommerce.pago.dto.PagoRequest;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.model.PagoItem;
import com.tienda.ecommerce.pago.repository.PagoRepository;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.service.RecomendacionService;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;
    private final RecomendacionService recomendacionService;
    private final ReciboPdfService reciboPdfService;
    private final EmailService emailService;
    private final ProductoRepository productoRepository;

    public PagoService(PagoRepository pagoRepository, CarritoService carritoService,
                       UsuarioRepository usuarioRepository, RecomendacionService recomendacionService,
                       ReciboPdfService reciboPdfService, EmailService emailService,
                       ProductoRepository productoRepository) {
        this.pagoRepository = pagoRepository;
        this.carritoService = carritoService;
        this.usuarioRepository = usuarioRepository;
        this.recomendacionService = recomendacionService;
        this.reciboPdfService = reciboPdfService;
        this.emailService = emailService;
        this.productoRepository = productoRepository;
    }

    public List<Pago> historialPorUsuario(Long usuarioId) {
    return pagoRepository.findByUsuarioId(usuarioId);
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
        pago.setDireccionEnvio(request.getDireccionEnvio());


        for (Carrito item : items) {
            PagoItem pagoItem = new PagoItem();
            pagoItem.setPago(pago);
            pagoItem.setNombreProducto(item.getProducto().getNombre());
            pagoItem.setPrecioUnitario(item.getProducto().getPrecio());
            pagoItem.setCantidad(item.getCantidad());
            pagoItem.setUrlImagen(item.getProducto().getUrlImagen());
            pago.getItems().add(pagoItem);
        }
        // 4. Actualizar stock de productos comprados
        for (Carrito item : items) {
            var producto = item.getProducto();
            int nuevaCantidad = (producto.getStock() == null ? 0 : producto.getStock()) - item.getCantidad();
            producto.setStock(Math.max(0, nuevaCantidad));
            productoRepository.save(producto);
        }
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

        // 5. Guardar el pago y luego enviar el recibo (antes de vaciar el carrito
        // para conservar los items que se usarán al armar el PDF).
        Pago pagoGuardado = pagoRepository.save(pago);
        enviarReciboPorCorreo(pagoGuardado, items);

        // 6. Vaciar el carrito
        carritoService.vaciarCarrito(usuario.getId());
        return pagoGuardado;
    }

    private void enviarReciboPorCorreo(Pago pago, List<Carrito> items) {
        Usuario usuario = pago.getUsuario();
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            log.warn("Usuario {} sin correo registrado; se omite el envío del recibo", usuario.getId());
            return;
        }
        try {
            byte[] pdf = reciboPdfService.generarRecibo(pago, items);
            emailService.enviarReciboCompra(usuario.getEmail(), usuario.getNombre(), pago.getId(), pdf);
        } catch (Exception e) {
            // El pago ya fue procesado; un fallo en el correo no debe revertirlo.
            log.error("No se pudo preparar/enviar el recibo del pago {}: {}", pago.getId(), e.getMessage(), e);
        }
    }
}
