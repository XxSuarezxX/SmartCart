package com.tienda.ecommerce.admin.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.tienda.ecommerce.admin.dto.AdminDashboardDTO;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.repository.PagoRepository;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;

@Service
public class AdminService {

    private final PagoRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminService(PagoRepository pagoRepository,
                        ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository) {
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public AdminDashboardDTO getDashboard() {
        List<Pago> todosPagos = pagoRepository.findAll();
        List<Usuario> todosClientes = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRol() == Usuario.Rol.CLIENTE)
                .toList();

        long totalClientes = todosClientes.size();
        long totalProductos = productoRepository.count();
        long totalPagos = todosPagos.size();
        double ingresosTotales = todosPagos.stream()
                .filter(p -> "EXITOSO".equals(p.getEstado()))
                .mapToDouble(Pago::getMontoTotal)
                .sum();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<AdminDashboardDTO.PagoResumenDTO> pagosRecientes = todosPagos.stream()
                .sorted((a, b) -> b.getFechaPago().compareTo(a.getFechaPago()))
                .limit(5)
                .map(p -> new AdminDashboardDTO.PagoResumenDTO(
                        p.getId(),
                        p.getUsuario().getNombre(),
                        p.getUsuario().getEmail(),
                        p.getMontoTotal(),
                        p.getEstado(),
                        p.getFechaPago().format(fmt)
                ))
                .toList();

        List<Usuario> clientesOrdenados = todosClientes.stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .toList();

        List<AdminDashboardDTO.ClienteResumenDTO> clientesRecientes = clientesOrdenados.stream()
                .limit(5)
                .map(u -> {
                    List<Pago> pagosUsuario = todosPagos.stream()
                            .filter(p -> p.getUsuario().getId().equals(u.getId()))
                            .toList();
                    double gastado = pagosUsuario.stream()
                            .filter(p -> "EXITOSO".equals(p.getEstado()))
                            .mapToDouble(Pago::getMontoTotal).sum();
                    return new AdminDashboardDTO.ClienteResumenDTO(
                            u.getId(), u.getNombre(), u.getEmail(),
                            pagosUsuario.size(), gastado
                    );
                })
                .toList();

        List<AdminDashboardDTO.ActividadDTO> actividadReciente = Stream.concat(
                pagosRecientes.stream().map(p -> new AdminDashboardDTO.ActividadDTO(
                        "COMPRA",
                        "Compra de " + p.getClienteNombre(),
                        "Monto: " + String.format("%.2f", p.getMonto()) + " - Estado: " + p.getEstado(),
                        p.getFecha()
                )),
                clientesRecientes.stream().map(c -> new AdminDashboardDTO.ActividadDTO(
                        "CLIENTE",
                        "Nuevo cliente: " + (c.getNombre() != null && !c.getNombre().isBlank() ? c.getNombre() : c.getEmail()),
                        "Total gastado: " + String.format("%.2f", c.getTotalGastado()),
                        "Reciente"
                ))
        ).limit(8).toList();

        // Calcular productos más vendidos a partir de los pagos
        Map<String, Long> ventasPorProducto = todosPagos.stream()
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(
                        i -> i.getNombreProducto(), Collectors.summingLong(i -> i.getCantidad())
                ));

        List<AdminDashboardDTO.TopProductoDTO> topProductos = ventasPorProducto.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> new AdminDashboardDTO.TopProductoDTO(e.getKey(),
                        // intentar obtener una imagen desde los pagos
                        todosPagos.stream()
                                .flatMap(p -> p.getItems().stream())
                                .filter(it -> it.getNombreProducto().equals(e.getKey()))
                                .map(it -> it.getUrlImagen())
                                .filter(u -> u != null && !u.isBlank())
                                .findFirst().orElse(null),
                        e.getValue()))
                .toList();

        return new AdminDashboardDTO(
                totalClientes, totalProductos, totalPagos,
                ingresosTotales, pagosRecientes, clientesRecientes, actividadReciente, topProductos
        );
    }
}