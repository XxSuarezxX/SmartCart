package com.tienda.ecommerce.recomendacion.repository;

import com.tienda.ecommerce.recomendacion.model.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteraccionRepository extends JpaRepository<Interaccion, Long> {
    // Query para sumar puntos agrupados por categoría para un usuario
    @Query("SELECT i.categoriaId, SUM(i.puntos) FROM Interaccion i WHERE i.usuarioId = ?1 GROUP BY i.categoriaId ORDER BY SUM(i.puntos) DESC")
    List<Object[]> findPreferenciasPorUsuario(Long usuarioId);

    @Query("SELECT i.categoriaId, SUM(i.puntos) FROM Interaccion i GROUP BY i.categoriaId ORDER BY SUM(i.puntos) DESC")
    List<Object[]> findRankingCategoriasGlobal();

    // ¿El usuario ya tiene una interacción de este tipo sobre este producto?
    boolean existsByUsuarioIdAndProductoIdAndTipo(Long usuarioId, Long productoId, String tipo);

    // Elimina las interacciones de un tipo (ej: LIKE) de un producto para un usuario
    void deleteByUsuarioIdAndProductoIdAndTipo(Long usuarioId, Long productoId, String tipo);

    // IDs de productos sobre los que el usuario tiene una interacción de cierto tipo
    @Query("SELECT DISTINCT i.productoId FROM Interaccion i WHERE i.usuarioId = ?1 AND i.tipo = ?2 AND i.productoId IS NOT NULL")
    List<Long> findProductoIdsByUsuarioYTipo(Long usuarioId, String tipo);
}