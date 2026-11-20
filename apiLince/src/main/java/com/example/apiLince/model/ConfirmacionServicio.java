package com.example.apiLince.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "confirmaciones_servicio")
public class ConfirmacionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String servicioId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioLocal usuario;

    @Column(nullable = false)
    private String estado; // "CONFIRMADO" o "RECHAZADO"

    @Column
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    public ConfirmacionServicio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getServicioId() { return servicioId; }
    public void setServicioId(String servicioId) { this.servicioId = servicioId; }
    public UsuarioLocal getUsuario() { return usuario; }
    public void setUsuario(UsuarioLocal usuario) { this.usuario = usuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}