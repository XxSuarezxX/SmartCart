package com.tienda.ecommerce.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardDTO {
    private long totalClientes;
    private long totalProductos;
    private long totalPagos;
    private double ingresosTotales;
    private List<PagoResumenDTO> pagosRecientes;
    private List<ClienteResumenDTO> clientesRecientes;
    private List<ActividadDTO> actividadReciente;
    private List<TopProductoDTO> topProductos;

    @Getter @AllArgsConstructor
    public static class PagoResumenDTO {
        private Long id;
        private String clienteNombre;
        private String clienteEmail;
        private Double monto;
        private String estado;
        private String fecha;
    }

    @Getter @AllArgsConstructor
    public static class ClienteResumenDTO {
        private Long id;
        private String nombre;
        private String email;
        private long totalPagos;
        private double totalGastado;
    }

    @Getter @AllArgsConstructor
    public static class ActividadDTO {
        private String tipo;
        private String mensaje;
        private String detalle;
        private String fecha;
    }

    @Getter @AllArgsConstructor
    public static class TopProductoDTO {
        private String nombre;
        private String urlImagen;
        private long vendidos;
    }
}