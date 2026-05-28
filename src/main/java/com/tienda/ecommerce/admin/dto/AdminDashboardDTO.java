package com.tienda.ecommerce.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdminDashboardDTO {
    private long totalClientes;
    private long totalProductos;
    private long totalPagos;
    private double ingresosTotales;
    private List<PagoResumenDTO> pagosRecientes;
    private List<ClienteResumenDTO> clientesRecientes;

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
}