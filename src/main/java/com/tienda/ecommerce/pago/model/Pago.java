package com.tienda.ecommerce.pago.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.tienda.ecommerce.usuarios.model.Usuario;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Double montoTotal;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false)
    private String estado; // "EXITOSO", "RECHAZADO"
}