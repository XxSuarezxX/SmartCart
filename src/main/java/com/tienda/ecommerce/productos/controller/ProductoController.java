package com.tienda.ecommerce.productos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tienda.ecommerce.productos.model.Categoria;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.service.ProductoService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return productoService.listarCategorias();
    }

    @PostMapping("/categorias")
    public Categoria crearCategoria(@RequestBody Categoria categoria, HttpServletRequest request) {
        // Loggar autenticación para depuración de 403
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG] crearCategoria - auth=" + auth);
        if (auth != null) {
            auth.getAuthorities().forEach(a -> System.out.println("[DEBUG] Authority: " + a.getAuthority()));
        }
        System.out.println("[DEBUG] Authorization header: " + request.getHeader("Authorization"));
        return productoService.guardarCategoria(categoria);
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarTodos();
    }

    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        System.out.println("=== CREAR PRODUCTO LLAMADO ===");
        return productoService.guardarProducto(producto);
    }

    @PutMapping("/{id}")
    public Producto editarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        producto.setId(id);
        return productoService.guardarProducto(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    @PostMapping("/cargar-csv")
    public ResponseEntity<String> cargarCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo está vacío");
        }

        try {
            String resumen = productoService.cargarProductosDesdeCsv(file);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al cargar el CSV: " + e.getMessage());
        }
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<String> eliminarCategoria(@PathVariable Long id) {
        try {
            String resumen = productoService.eliminarCategoriaConProductos(id);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al eliminar categoría: " + e.getMessage());
        }
    }
}