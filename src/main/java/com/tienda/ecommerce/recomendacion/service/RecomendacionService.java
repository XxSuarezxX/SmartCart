package com.tienda.ecommerce.recomendacion.service;

import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.recomendacion.model.Interaccion;
import com.tienda.ecommerce.recomendacion.model.TipoInteraccion;
import com.tienda.ecommerce.recomendacion.repository.InteraccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecomendacionService {

    private final InteraccionRepository interaccionRepository;
    private final ProductoRepository productoRepository;

    public RecomendacionService(InteraccionRepository interaccionRepository,
                                ProductoRepository productoRepository) {
        this.interaccionRepository = interaccionRepository;
        this.productoRepository = productoRepository;
    }

    public Interaccion guardarInteraccion(Interaccion interaccion) {
        TipoInteraccion tipo = interaccion.getTipo();

        // Un LIKE es un toggle por producto: si ya existe, no lo duplicamos.
        if (tipo == TipoInteraccion.LIKE && interaccion.getProductoId() != null
                && interaccionRepository.existsByUsuarioIdAndProductoIdAndTipo(
                        interaccion.getUsuarioId(), interaccion.getProductoId(), TipoInteraccion.LIKE)) {
            return interaccion;
        }

        // Los puntos son una propiedad del propio tipo (principio Abierto/Cerrado).
        interaccion.setPuntos(tipo == null ? 0 : tipo.getPuntos());
        return interaccionRepository.save(interaccion);
    }

    @Transactional
    public void quitarLike(Long usuarioId, Long productoId) {
        interaccionRepository.deleteByUsuarioIdAndProductoIdAndTipo(usuarioId, productoId, TipoInteraccion.LIKE);
    }

    public List<Producto> obtenerLikes(Long usuarioId) {
        List<Long> productoIds = interaccionRepository.findProductoIdsByUsuarioYTipo(usuarioId, TipoInteraccion.LIKE);
        return productoRepository.findAllById(productoIds);
    }

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