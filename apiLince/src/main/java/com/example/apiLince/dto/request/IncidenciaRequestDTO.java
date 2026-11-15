package cl.duoc.linceops.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class IncidenciaRequestDTO {

    @NotBlank(message = "El ID del servicio es obligatorio")
    private String servicioId;

    @NotBlank(message = "El motivo de la incidencia es obligatorio")
    private String motivo; // ej: "FUERA_DE_RADIO", "ATRASO"

    private String detalle; // Opcional, para que el conductor explique

    // Getters y Setters
    public String getServicioId() { return servicioId; }
    public void setServicioId(String servicioId) { this.servicioId = servicioId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}