package com.tienda.ecommerce.pago.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.tienda.ecommerce.usuarios.model.Usuario;
import java.time.LocalDateTime;
import com.tienda.ecommerce.pago.model.PagoItem;
import jakarta.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado;

    private String direccionEnvio;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PagoItem> items = new ArrayList<>();
}