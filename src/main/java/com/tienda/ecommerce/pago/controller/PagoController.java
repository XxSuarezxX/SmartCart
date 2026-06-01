package com.tienda.ecommerce.pago.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tienda.ecommerce.pago.dto.PagoRequest;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.service.PagoService;

// La política de CORS está centralizada en SeguridadConfig (lista blanca de orígenes).
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/procesar")
    public Pago realizarPago(@RequestBody PagoRequest request) {
        return pagoService.procesarPago(request);
    }

    @GetMapping("/historial/{usuarioId}")
    public List<Pago> historial(@PathVariable Long usuarioId) {
        return pagoService.historialPorUsuario(usuarioId);
    }

    @GetMapping("/todos")
    public List<Pago> todos() {
        return pagoService.listarTodos();
    }
}