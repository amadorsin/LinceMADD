package cl.duoc.linceops.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bloqueos_calendario")
public class BloqueoCalendario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioLocal usuario;

    @Column(nullable = false)
    private LocalDate fechaBloqueada;

    @Column(nullable = false)
    private LocalDate fechaSolicitud; // Para validar la regla de los 7 días de anticipación

    // Constructores, Getters y Setters
    public BloqueoCalendario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsuarioLocal getUsuario() { return usuario; }
    public void setUsuario(UsuarioLocal usuario) { this.usuario = usuario; }
    public LocalDate getFechaBloqueada() { return fechaBloqueada; }
    public void setFechaBloqueada(LocalDate fechaBloqueada) { this.fechaBloqueada = fechaBloqueada; }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}