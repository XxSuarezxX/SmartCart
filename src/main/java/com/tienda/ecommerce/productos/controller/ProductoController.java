package com.tienda.ecommerce.productos.controller;

import com.tienda.ecommerce.productos.model.Categoria;
import com.tienda.ecommerce.productos.model.Producto;
import com.tienda.ecommerce.productos.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Categoria crearCategoria(@RequestBody Categoria categoria) {
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
}