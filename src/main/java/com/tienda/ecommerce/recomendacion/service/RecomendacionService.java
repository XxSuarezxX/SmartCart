package com.tienda.ecommerce.recomendacion.service;

import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.repository.InteraccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecomendacionService {

    @Autowired
    private InteraccionRepository interaccionRepository;

    public Interaccion guardarInteraccion(Interaccion interaccion) {
        // La lógica de negocio se queda aquí
        int puntos = switch (interaccion.getTipo().toUpperCase()) {
            case "LIKE" -> 1;
            case "CARRITO" -> 2;
            case "COMPRA" -> 3;
            default -> 0;
        };

        interaccion.setPuntos(puntos);
        return interaccionRepository.save(interaccion);
    }

    @Autowired
    private ProductoRepository productoRepository; // Inyecta tu repositorio de productos

    public List<Producto> obtenerRecomendaciones(Long usuarioId) {
        List<Object[]> preferencias = interaccionRepository.findPreferenciasPorUsuario(usuarioId);

        if (preferencias.isEmpty()) {
            // Si no tiene interacciones, devolvemos productos aleatorios o los más nuevos
            return productoRepository.findAll().stream().limit(5).toList();
        }

        // El primer elemento [0] es la categoría con más puntos gracias al ORDER BY DESC
        Long categoriaFavoritaId = (Long) preferencias.get(0)[0];

        // Retornamos los productos de esa categoría
        return productoRepository.findByCategoriaId(categoriaFavoritaId);
    }

    public List<Object[]> obtenerRankingGlobal() {
        return interaccionRepository.findRankingCategoriasGlobal();
    }
}