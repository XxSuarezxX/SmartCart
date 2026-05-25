package com.tienda.ecommerce.carrito.controller;

import com.tienda.ecommerce.carrito.dto.CarritoRequest;
import com.tienda.ecommerce.carrito.dto.CarritoResponse;
import com.tienda.ecommerce.carrito.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{usuarioId}")
    public List<CarritoResponse> obtenerCarrito(@PathVariable Long usuarioId) {
        return carritoService.obtenerCarrito(usuarioId).stream()
                .map(CarritoResponse::from)
                .toList();
    }

    @PostMapping("/{usuarioId}/agregar")
    public CarritoResponse agregarProducto(@PathVariable Long usuarioId,
                                           @RequestBody CarritoRequest request) {
        return CarritoResponse.from(
                carritoService.agregarProducto(usuarioId, request.getProductoId(), request.getCantidad()));
    }

    @PutMapping("/item/{carritoId}")
    public CarritoResponse actualizarCantidad(@PathVariable Long carritoId,
                                              @RequestBody CarritoRequest request) {
        return CarritoResponse.from(
                carritoService.actualizarCantidad(carritoId, request.getCantidad()));
    }

    @DeleteMapping("/item/{carritoId}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long carritoId) {
        carritoService.eliminarProducto(carritoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}/vaciar")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
