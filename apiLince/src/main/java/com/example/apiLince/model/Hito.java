package cl.duoc.linceops.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hitos")
public class Hito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "servicio_id", nullable = false)
    private String servicioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioLocal usuario;

    @Column(nullable = false)
    private String tipo; // "CHECK_IN" o "CHECK_OUT"

    @Column(nullable = false)
    private LocalDateTime timestampRegistro;

    @Column(nullable = false)
    private Double latitudMarcaje;

    @Column(nullable = false)
    private Double longitudMarcaje;

    // Constructores, Getters y Setters
    public Hito() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getServicioId() { return servicioId; }
    public void setServicioId(String servicioId) { this.servicioId = servicioId; }
    public UsuarioLocal getUsuario() { return usuario; }
    public void setUsuario(UsuarioLocal usuario) { this.usuario = usuario; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getTimestampRegistro() { return timestampRegistro; }
    public void setTimestampRegistro(LocalDateTime timestampRegistro) { this.timestampRegistro = timestampRegistro; }
    public Double getLatitudMarcaje() { return latitudMarcaje; }
    public void setLatitudMarcaje(Double latitudMarcaje) { this.latitudMarcaje = latitudMarcaje; }
    public Double getLongitudMarcaje() { return longitudMarcaje; }
    public void setLongitudMarcaje(Double longitudMarcaje) { this.longitudMarcaje = longitudMarcaje; }
}