package com.tienda.ecommerce.recomendacion.controller;

import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.recomendacion.dto.InteraccionDTO;
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
    public Interaccion registrar(@RequestBody InteraccionDTO dto) {
        // Construimos la entidad solo con los campos que el cliente puede definir.
        // Así evitamos que se inyecten id, puntos o fecha desde el JSON.
        Interaccion nuevaInteraccion = new Interaccion();
        nuevaInteraccion.setUsuarioId(dto.getUsuarioId());
        nuevaInteraccion.setCategoriaId(dto.getCategoriaId());
        nuevaInteraccion.setTipo(dto.getTipo());

        // Los puntos los calcula el servicio a partir del tipo.
        return recomendacionService.guardarInteraccion(nuevaInteraccion);
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