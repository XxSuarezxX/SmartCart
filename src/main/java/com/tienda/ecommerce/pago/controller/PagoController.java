package com.tienda.ecommerce.pago.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tienda.ecommerce.pago.dto.PagoRequest;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.service.PagoService;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*") // Para que tu compañero del front no tenga líos de CORS
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
}