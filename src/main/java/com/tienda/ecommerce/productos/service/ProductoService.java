package com.tienda.ecommerce.productos.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tienda.ecommerce.common.exception.RecursoNoEncontradoException;
import com.tienda.ecommerce.productos.csv.FilaProductoCsv;
import com.tienda.ecommerce.productos.csv.ProductoCsvParser;
import com.tienda.ecommerce.productos.csv.ResultadoParseoCsv;
import com.tienda.ecommerce.productos.model.Categoria;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.CategoriaRepository;
import com.tienda.ecommerce.productos.repository.ProductoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoCsvParser productoCsvParser;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoCsvParser productoCsvParser) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoCsvParser = productoCsvParser;
    }

    // --- LÓGICA DE CATEGORÍAS ---

    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        Long idLibre = buscarPrimerIdLibre();
        entityManager.createNativeQuery(
            "INSERT INTO categorias (id, nombre) VALUES (:id, :nombre)")
            .setParameter("id", idLibre)
            .setParameter("nombre", categoria.getNombre())
            .executeUpdate();
        categoria.setId(idLibre);
        return categoria;
    }

    private Long buscarPrimerIdLibre() {
        long idLibre = 1L;
        List<Long> ids = categoriaRepository.findAll().stream()
                .map(Categoria::getId)
                .filter(id -> id != null && id > 0)
                .sorted()
                .toList();

        for (Long id : ids) {
            if (id > idLibre) break;
            if (id.equals(idLibre)) idLibre++;
        }
        return idLibre;
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll().stream()
            .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
            .toList();
    }

    // --- LÓGICA DE PRODUCTOS ---

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        entityManager.createNativeQuery("DELETE FROM carrito WHERE producto_id = :id")
            .setParameter("id", id)
            .executeUpdate();
        productoRepository.deleteById(id);
    }

    public String eliminarCategoriaConProductos(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + categoriaId));

        List<Producto> productos = productoRepository.findByCategoriaId(categoriaId);
        List<String> nombresProductos = productos.stream().map(Producto::getNombre).toList();

        productoRepository.deleteAll(productos);
        categoriaRepository.delete(categoria);

        StringBuilder resumen = new StringBuilder();
        resumen.append("Categoría '").append(categoria.getNombre()).append("' eliminada.");
        resumen.append(" Se eliminaron ").append(productos.size()).append(" producto(s).");
        if (!nombresProductos.isEmpty()) {
            resumen.append(" Productos: ").append(String.join(", ", nombresProductos));
        }
        return resumen.toString();
    }

    // --- CARGA MASIVA DESDE CSV ---
    // El parseo del archivo lo hace ProductoCsvParser (SRP); aquí solo
    // resolvemos las categorías y persistimos los productos válidos.

    @Transactional
    public String cargarProductosDesdeCsv(MultipartFile archivo) {
        ResultadoParseoCsv parseo = productoCsvParser.parse(archivo);

        List<String> errores = new ArrayList<>(parseo.errores());
        List<Producto> productos = new ArrayList<>();

        for (FilaProductoCsv fila : parseo.filas()) {
            Categoria categoria = categoriaRepository.findById(fila.categoriaId()).orElse(null);
            if (categoria == null) {
                errores.add("Línea " + fila.numeroLinea() + ": categoría no encontrada " + fila.categoriaId());
                continue;
            }

            Producto producto = new Producto();
            producto.setDescripcion(fila.descripcion());
            producto.setNombre(fila.nombre());
            producto.setPrecio(fila.precio());
            producto.setStock(fila.stock());
            producto.setUrlImagen(fila.urlImagen());
            producto.setCategoria(categoria);
            producto.setColores(fila.colores());
            producto.setTallas(fila.tallas());

            productos.add(producto);
        }

        productoRepository.saveAll(productos);

        StringBuilder resumen = new StringBuilder();
        resumen.append("Productos cargados: ").append(productos.size());
        if (!errores.isEmpty()) {
            resumen.append(". No se cargaron ")
                    .append(errores.size())
                    .append(" registros: ")
                    .append(String.join("; ", errores));
        }
        return resumen.toString();
    }
}