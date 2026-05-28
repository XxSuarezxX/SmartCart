package com.tienda.ecommerce.admin.service;

import com.tienda.ecommerce.admin.dto.AdminDashboardDTO;
import com.tienda.ecommerce.pago.model.Pago;
import com.tienda.ecommerce.pago.repository.PagoRepository;
import com.tienda.ecommerce.productos.repository.ProductoRepository;
import com.tienda.ecommerce.usuarios.model.Usuario;
import com.tienda.ecommerce.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

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

        List<AdminDashboardDTO.ClienteResumenDTO> clientesRecientes = todosClientes.stream()
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

        return new AdminDashboardDTO(
                totalClientes, totalProductos, totalPagos,
                ingresosTotales, pagosRecientes, clientesRecientes
        );
    }
}