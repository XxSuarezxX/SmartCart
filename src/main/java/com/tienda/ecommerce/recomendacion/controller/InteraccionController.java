package com.tienda.ecommerce.recomendacion.controller;

import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.service.RecomendacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interacciones")
public class InteraccionController {

    @Autowired
    private RecomendacionService recomendacionService;

    @PostMapping("/registrar")
    public Interaccion registrar(@RequestBody Interaccion interaccion) {
        return recomendacionService.guardarInteraccion(interaccion);
    }

    @GetMapping("/sugeridos/{usuarioId}")
    public List<Producto> obtenerSugeridos(@PathVariable Long usuarioId) {
        return recomendacionService.obtenerRecomendaciones(usuarioId);
    }

    @GetMapping("/admin/ranking")
    public List<Object[]> obtenerRanking() {
        return recomendacionService.obtenerRankingGlobal();
    }
}