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
}