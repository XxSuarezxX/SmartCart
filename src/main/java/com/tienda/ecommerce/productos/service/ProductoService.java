package com.tienda.ecommerce.productos.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tienda.ecommerce.productos.model.Categoria;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.repository.CategoriaRepository;
import com.tienda.ecommerce.productos.repository.ProductoRepository;

@Service
public class ProductoService {


    private final ProductoRepository productoRepository;

    @Autowired
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // --- LÓGICA DE CATEGORÍAS ---

    public Categoria guardarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    // --- LÓGICA DE PRODUCTOS ---

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto guardarProducto(Producto producto) {
        // Aquí podríamos validar si la categoría existe antes de guardar
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional
    public String cargarProductosDesdeCsv(MultipartFile archivo) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean primeraLinea = true;
            List<Producto> productos = new ArrayList<>();
            List<String> errores = new ArrayList<>();
            int numeroLinea = 0;

            while ((linea = fileReader.readLine()) != null) {
                numeroLinea++;
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String[] datos = linea.split(",");
                if (datos.length < 8) {
                    errores.add("Línea " + numeroLinea + ": formato inválido");
                    continue;
                }

                try {
                    String descripcion = datos[0].trim();
                    String nombre = datos[1].trim();
                    Double precio = Double.parseDouble(datos[2].trim());
                    int stock = Integer.parseInt(datos[3].trim());
                    String urlImagen = datos[4].trim();
                    Long categoriaId = Long.parseLong(datos[5].trim());
                    String colores = datos[6].trim();
                    String tallas = datos[7].trim();

                    Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
                    if (categoria == null) {
                        errores.add("Línea " + numeroLinea + ": categoría no encontrada " + categoriaId);
                        continue;
                    }

                    Producto producto = new Producto();
                    producto.setDescripcion(descripcion);
                    producto.setNombre(nombre);
                    producto.setPrecio(precio);
                    producto.setStock(stock);
                    producto.setUrlImagen(urlImagen);
                    producto.setCategoria(categoria);
                    producto.setColores(colores);
                    producto.setTallas(tallas);

                    productos.add(producto);
                } catch (NumberFormatException e) {
                    errores.add("Línea " + numeroLinea + ": valores numéricos inválidos");
                }
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
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el archivo CSV: " + e.getMessage());
        }
    }
}