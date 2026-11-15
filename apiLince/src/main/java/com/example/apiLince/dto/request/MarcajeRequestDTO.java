package cl.duoc.linceops.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class MarcajeRequestDTO {

    @NotBlank(message = "El ID del servicio es obligatorio")
    private String servicioId;

    @NotBlank(message = "El tipo de marcaje es obligatorio (CHECK_IN o CHECK_OUT)")
    private String tipo;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    @NotNull(message = "El timestamp es obligatorio")
    private LocalDateTime timestamp; // La app móvil debe mandar la hora exacta en la que se presionó el botón

    // Getters y Setters
    public String getServicioId() { return servicioId; }
    public void setServicioId(String servicioId) { this.servicioId = servicioId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}