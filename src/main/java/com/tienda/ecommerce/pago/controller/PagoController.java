package com.tienda.ecommerce.pago.controller;

import com.tienda.ecommerce.pago.dto.PagoRequest;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.service.PagoService;
import org.springframework.web.bind.annotation.*;

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
}